package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.domain.InspectCheckResult;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.InspectTemplate;
import com.swmanager.system.domain.InspectVisitLog;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.dto.InspectVisitLogDTO;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.*;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.service.ops.OpsDocLinkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
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

    // ===== save (수정 경로 + QR 보호 + visits + COMPLETED) =====

    /** save 최종 return findById(reportId) 가 통과하도록 공통 stub(pjtId=null → previousVisits 경로 회피). */
    private void stubFinalFindById(long reportId, DocumentStatus status) {
        InspectReport finalReport = new InspectReport();
        finalReport.setId(reportId);
        finalReport.setStatus(status);   // pjtId/inspectMonth null → findPreviousVisitsByProject 미호출
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(finalReport));
        when(visitLogRepository.findByReportIdOrderBySortOrderAsc(reportId)).thenReturn(List.of());
        when(checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(reportId)).thenReturn(List.of());
    }

    @Test
    void save_update_preservesAudit_deletesVisitLogs_noQrProtection() {
        SecurityContextHolder.clearContext();
        InspectReportDTO dto = new InspectReportDTO();
        dto.setId(10L);
        dto.setStatus(DocumentStatus.DRAFT);
        InspectCheckResultDTO incoming = new InspectCheckResultDTO();
        incoming.setSection("A");                 // resultCode 없음 → incomingWithCode 비어있음
        dto.setCheckResults(List.of(incoming));

        InspectReport existing = new InspectReport();
        existing.setId(10L);
        existing.setCreatedBy("orig-user");
        existing.setCreatedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        existing.setStatus(DocumentStatus.DRAFT);
        when(reportRepository.findById(10L)).thenReturn(Optional.of(existing));   // L62 기존 조회 + 최종 findById
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> i.getArgument(0));
        // 기존 checkResult: resultCode 없음 → qrSections 비어있음 → 보호 없음
        InspectCheckResult ec = new InspectCheckResult();
        ec.setSection("A"); ec.setResultCode(null);
        when(checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(10L)).thenReturn(List.of(ec));
        when(visitLogRepository.findByReportIdOrderBySortOrderAsc(10L)).thenReturn(List.of());

        InspectReportDTO out = service.save(dto);

        assertThat(out.getId()).isEqualTo(10L);
        verify(visitLogRepository).deleteByReportId(10L);
        verify(checkResultRepository).deleteByReportId(10L);                              // 보호 없음 → 전체 삭제
        verify(checkResultRepository, never()).deleteByReportIdAndSectionIn(anyLong(), anyList());
        ArgumentCaptor<InspectReport> cap = ArgumentCaptor.forClass(InspectReport.class);
        verify(reportRepository).save(cap.capture());
        assertThat(cap.getValue().getCreatedBy()).isEqualTo("orig-user");                // 기존 보존
        assertThat(cap.getValue().getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 9, 0));
        assertThat(cap.getValue().getUpdatedBy()).isEqualTo("system");
        verify(checkResultRepository).save(any(InspectCheckResult.class));               // incoming "A" 저장
    }

    @Test
    void save_update_qrProtection_keepsProtectedAndSkipsIncoming() {
        SecurityContextHolder.clearContext();
        InspectReportDTO dto = new InspectReportDTO();
        dto.setId(20L);
        dto.setStatus(DocumentStatus.DRAFT);
        InspectCheckResultDTO inQr = new InspectCheckResultDTO();  inQr.setSection("QR_SEC");  // 코드없음 → 보호 유지
        InspectCheckResultDTO inX  = new InspectCheckResultDTO();  inX.setSection("X");        // 저장 대상
        dto.setCheckResults(List.of(inQr, inX));

        InspectReport existing = new InspectReport();
        existing.setId(20L); existing.setCreatedBy("orig"); existing.setStatus(DocumentStatus.DRAFT);
        when(reportRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> i.getArgument(0));
        // 기존: QR_SEC(resultCode 있음→QR 적재) + PLAIN(없음)
        InspectCheckResult qr = new InspectCheckResult();    qr.setSection("QR_SEC"); qr.setResultCode("OK");
        InspectCheckResult plain = new InspectCheckResult(); plain.setSection("PLAIN"); plain.setResultCode(null);
        when(checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(20L)).thenReturn(List.of(qr, plain));
        when(visitLogRepository.findByReportIdOrderBySortOrderAsc(20L)).thenReturn(List.of());

        service.save(dto);

        verify(checkResultRepository, never()).deleteByReportId(20L);                     // 보호 발생 → 전체삭제 안 함
        ArgumentCaptor<List<String>> secCap = ArgumentCaptor.forClass(List.class);
        verify(checkResultRepository).deleteByReportIdAndSectionIn(eq(20L), secCap.capture());
        assertThat(secCap.getValue()).containsExactlyInAnyOrder("PLAIN");                 // QR_SEC 보호, PLAIN 만 삭제
        ArgumentCaptor<InspectCheckResult> savedCap = ArgumentCaptor.forClass(InspectCheckResult.class);
        verify(checkResultRepository).save(savedCap.capture());                           // QR_SEC skip, X 만 저장
        assertThat(savedCap.getValue().getSection()).isEqualTo("X");
    }

    @Test
    void save_savesVisits_withSortOrderAssigned() {
        SecurityContextHolder.clearContext();
        InspectReportDTO dto = new InspectReportDTO();
        dto.setStatus(DocumentStatus.DRAFT);                  // 신규(id=null)
        InspectVisitLogDTO v1 = new InspectVisitLogDTO();     // sortOrder null → 1
        InspectVisitLogDTO v2 = new InspectVisitLogDTO(); v2.setSortOrder(0);   // 0 → 2
        dto.setVisits(List.of(v1, v2));

        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> {
            InspectReport r = i.getArgument(0); r.setId(70L); return r;
        });
        stubFinalFindById(70L, DocumentStatus.DRAFT);

        service.save(dto);

        ArgumentCaptor<InspectVisitLog> cap = ArgumentCaptor.forClass(InspectVisitLog.class);
        verify(visitLogRepository, times(2)).save(cap.capture());
        assertThat(cap.getAllValues()).extracting(InspectVisitLog::getSortOrder).containsExactly(1, 2);
    }

    @Test
    void save_completed_linksOpsDoc() {
        SecurityContextHolder.clearContext();
        InspectReportDTO dto = new InspectReportDTO();
        dto.setStatus(DocumentStatus.COMPLETED);             // 신규 + COMPLETED → 연계
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> {
            InspectReport r = i.getArgument(0); r.setId(60L); return r;
        });
        stubFinalFindById(60L, DocumentStatus.COMPLETED);

        service.save(dto);

        verify(opsDocLinkService).linkInspectReport(any(InspectReport.class));
    }
}
