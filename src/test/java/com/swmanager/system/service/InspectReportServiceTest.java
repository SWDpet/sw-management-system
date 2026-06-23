package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.InspectTemplate;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.*;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.service.ops.OpsDocLinkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * InspectReportService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 파괴적 resetAllInspectData 는 커버 타겟 제외(codex).
 */
class InspectReportServiceTest {

    private final InspectReportRepository reportRepository = mock(InspectReportRepository.class);
    private final InspectVisitLogRepository visitLogRepository = mock(InspectVisitLogRepository.class);
    private final InspectCheckResultRepository checkResultRepository = mock(InspectCheckResultRepository.class);
    private final InspectTemplateRepository templateRepository = mock(InspectTemplateRepository.class);
    private final InspectQrBatchRepository qrBatchRepository = mock(InspectQrBatchRepository.class);
    private final OpsDocumentRepository opsDocumentRepository = mock(OpsDocumentRepository.class);
    private final InspectMetricSnapshotRepository metricSnapshotRepository = mock(InspectMetricSnapshotRepository.class);
    private final OpsDocLinkService opsDocLinkService = mock(OpsDocLinkService.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private final InspectReportService service = new InspectReportService(
            reportRepository, visitLogRepository, checkResultRepository, templateRepository,
            qrBatchRepository, opsDocumentRepository, metricSnapshotRepository, opsDocLinkService, messages);

    // ===== getPreviousVisits =====

    @Test
    void getPreviousVisits_nullOrBlank_returnsEmpty() {
        assertThat(service.getPreviousVisits(null, "2026-05")).isEmpty();
        assertThat(service.getPreviousVisits(1L, null)).isEmpty();
        assertThat(service.getPreviousVisits(1L, "")).isEmpty();
        verify(visitLogRepository, never()).findPreviousVisitsByProject(any(), any());
    }

    @Test
    void getPreviousVisits_valid_delegates() {
        when(visitLogRepository.findPreviousVisitsByProject(1L, "2026-05")).thenReturn(List.of());
        assertThat(service.getPreviousVisits(1L, "2026-05")).isEmpty();
        verify(visitLogRepository).findPreviousVisitsByProject(1L, "2026-05");
    }

    // ===== findByProjectAndMonth =====

    @Test
    void findByProjectAndMonth_nullInputs_returnsNull() {
        assertThat(service.findByProjectAndMonth(null, "2026-05")).isNull();
        assertThat(service.findByProjectAndMonth(1L, null)).isNull();
        assertThat(service.findByProjectAndMonth(1L, "")).isNull();
    }

    // ===== getTemplateItems =====

    @Test
    void getTemplateItems_mapsTemplateToDto() {
        InspectTemplate t = new InspectTemplate();
        t.setSection("서버"); t.setCategory("성능"); t.setItemName("CPU"); t.setItemMethod("육안"); t.setSortOrder(1);
        when(templateRepository.findByTemplateTypeAndUseYnOrderBySectionAscSortOrderAsc("UPIS", "Y"))
                .thenReturn(List.of(t));
        var items = service.getTemplateItems("UPIS");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getSection()).isEqualTo("서버");
        assertThat(items.get(0).getItemName()).isEqualTo("CPU");
        assertThat(items.get(0).getSortOrder()).isEqualTo(1);
    }

    // ===== findById =====

    @Test
    void findById_notFound_throws() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        when(messages.get(eq("error.inspect_report.not_found"), any())).thenReturn("없음");
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findById_softDeleted_throws() {
        InspectReport r = new InspectReport();
        r.setId(5L); r.setDeletedAt(LocalDateTime.now());
        when(reportRepository.findById(5L)).thenReturn(Optional.of(r));
        when(messages.get(anyString(), any())).thenReturn("없음");
        assertThatThrownBy(() -> service.findById(5L)).isInstanceOf(IllegalArgumentException.class);
    }

    // ===== findByProject =====

    @Test
    void findByProject_delegatesAndMaps() {
        InspectReport r = new InspectReport(); r.setId(1L);
        when(reportRepository.findByPjtIdAndDeletedAtIsNullOrderByCreatedAtDesc(7L)).thenReturn(List.of(r));
        assertThat(service.findByProject(7L)).hasSize(1);
    }

    // ===== delete (soft delete + ops doc cleanup) =====

    @Test
    void delete_softDeletes_andCleansOpsDocs() {
        InspectReport r = new InspectReport();
        r.setId(3L); r.setInspectMonth("2026-05");
        when(reportRepository.findById(3L)).thenReturn(Optional.of(r));
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> i.getArgument(0));
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());

        service.delete(3L);

        assertThat(r.getDeletedAt()).isNotNull();          // soft delete
        assertThat(r.getBatchId()).isNull();
        verify(reportRepository).save(r);
        verify(qrBatchRepository).deleteByReportId(3L);
        // INSP-2026-3 / INSP-3 / INSP-2026-05-3 3포맷 룩업
        verify(opsDocumentRepository).findByDocNo("INSP-2026-3");
        verify(opsDocumentRepository).findByDocNo("INSP-3");
        verify(opsDocumentRepository).findByDocNo("INSP-2026-05-3");
    }

    // ===== save (신규 보고서 — currentUser 폴백 + 비-COMPLETED) =====

    @Test
    void save_newReport_setsCreatedBy_andNoOpsLinkWhenDraft() {
        InspectReportDTO dto = new InspectReportDTO();
        dto.setStatus(DocumentStatus.DRAFT);   // not COMPLETED → opsDocLink 미호출
        // id=null, pjtId=null(previousVisits skip), visits/checkResults null

        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> {
            InspectReport r = i.getArgument(0);
            r.setId(50L);
            return r;
        });
        when(reportRepository.findById(50L)).thenAnswer(i -> {
            InspectReport r = new InspectReport(); r.setId(50L); r.setStatus(DocumentStatus.DRAFT);
            return Optional.of(r);
        });
        when(visitLogRepository.findByReportIdOrderBySortOrderAsc(50L)).thenReturn(List.of());
        when(checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(50L)).thenReturn(List.of());

        InspectReportDTO out = service.save(dto);

        assertThat(out.getId()).isEqualTo(50L);
        ArgumentCaptor<InspectReport> cap = ArgumentCaptor.forClass(InspectReport.class);
        verify(reportRepository).save(cap.capture());
        assertThat(cap.getValue().getCreatedBy()).isEqualTo("system");  // currentUser 폴백(컨텍스트 없음)
        assertThat(cap.getValue().getUpdatedBy()).isEqualTo("system");
        verify(opsDocLinkService, never()).linkInspectReport(any());    // DRAFT → 미연계
    }
}
