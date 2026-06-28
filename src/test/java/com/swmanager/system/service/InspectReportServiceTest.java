package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.InspectCheckResult;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.InspectTemplate;
import com.swmanager.system.domain.InspectVisitLog;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.dto.InspectVisitLogDTO;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.*;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.service.ops.OpsDocLinkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

/**
 * InspectReportService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * resetAllInspectData 도 커버 — 모든 repository 의존성을 mock 하므로 실 삭제 0(무해).
 * reset 이 wipe 하는 6개 저장소의 count 캡처·삭제 호출·순서(InOrder)를 박제해 위험 일괄삭제 회귀를 방어.
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

    // ===== A1: resetAllInspectData (mock 기반 — 실 삭제 0, 위험연산 박제) =====

    @Test
    void resetAllInspectData_capturesCountsThenDeletesAllStores() {
        List<OpsDocument> inspectDocs = List.of(new OpsDocument(), new OpsDocument());  // size 2
        when(opsDocumentRepository.findByDocTypeOrderByCreatedAtDesc(OpsDocType.INSPECT)).thenReturn(inspectDocs);
        when(checkResultRepository.count()).thenReturn(10L);
        when(visitLogRepository.count()).thenReturn(5L);
        when(qrBatchRepository.count()).thenReturn(3L);
        when(metricSnapshotRepository.count()).thenReturn(7L);
        when(reportRepository.count()).thenReturn(2L);

        Map<String, Long> counts = service.resetAllInspectData();

        // count map: 삭제 전 스냅샷 정확히 6키(잉여 키 회귀도 차단)
        assertThat(counts)
                .hasSize(6)
                .containsEntry("checkResult", 10L).containsEntry("visitLog", 5L)
                .containsEntry("qrBatch", 3L).containsEntry("metricSnapshot", 7L)
                .containsEntry("opsDocInspect", 2L).containsEntry("report", 2L);
        // 6개 저장소 wipe
        verify(checkResultRepository).deleteAllInBatch();
        verify(visitLogRepository).deleteAllInBatch();
        verify(qrBatchRepository).deleteAllInBatch();
        verify(metricSnapshotRepository).deleteAllInBatch();
        verify(opsDocumentRepository).deleteAll(inspectDocs);
        verify(reportRepository).deleteAllInBatch();
        // 계약 ①: count() 가 deleteAllInBatch() 보다 먼저(삭제 전 스냅샷)
        InOrder snap = inOrder(checkResultRepository);
        snap.verify(checkResultRepository).count();
        snap.verify(checkResultRepository).deleteAllInBatch();
        // 계약 ②: 삭제 순서 = 자식(checkResult→visitLog→qrBatch) → metricSnapshot → opsDoc → report
        //   (FK child-first, production L247 주석 계약). 다중 mock InOrder 로 전 순서 박제.
        InOrder order = inOrder(checkResultRepository, visitLogRepository, qrBatchRepository,
                metricSnapshotRepository, opsDocumentRepository, reportRepository);
        order.verify(checkResultRepository).deleteAllInBatch();
        order.verify(visitLogRepository).deleteAllInBatch();
        order.verify(qrBatchRepository).deleteAllInBatch();
        order.verify(metricSnapshotRepository).deleteAllInBatch();
        order.verify(opsDocumentRepository).deleteAll(inspectDocs);
        order.verify(reportRepository).deleteAllInBatch();
    }

    // ===== A2: findByProjectAndMonth found/not-found =====

    @Test
    void findByProjectAndMonth_found_returnsFullDtoViaFindById() {
        InspectReport report = new InspectReport();
        report.setId(9L); report.setPjtId(1L); report.setInspectMonth("2026-05");   // deletedAt=null
        when(reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull(1L, "2026-05"))
                .thenReturn(Optional.of(report));
        when(reportRepository.findById(9L)).thenReturn(Optional.of(report));         // findById 체인
        when(visitLogRepository.findByReportIdOrderBySortOrderAsc(9L)).thenReturn(List.of());
        when(checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(9L)).thenReturn(List.of());

        InspectReportDTO out = service.findByProjectAndMonth(1L, "2026-05");

        assertThat(out).isNotNull();
        assertThat(out.getId()).isEqualTo(9L);
        verify(reportRepository).findById(9L);   // 풀 DTO 경유 확인
    }

    @Test
    void findByProjectAndMonth_notFound_returnsNull() {
        when(reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull(1L, "2026-05"))
                .thenReturn(Optional.empty());
        assertThat(service.findByProjectAndMonth(1L, "2026-05")).isNull();
    }

    // ===== A3: save update — incoming 이 resultCode 보유(L80-81) → 해당 section 보호 해제 =====

    @Test
    void save_update_incomingResultCode_overridesProtection() {
        SecurityContextHolder.clearContext();
        InspectReportDTO dto = new InspectReportDTO();
        dto.setId(30L);
        dto.setStatus(DocumentStatus.DRAFT);
        InspectCheckResultDTO incoming = new InspectCheckResultDTO();
        incoming.setSection("AP"); incoming.setResultCode("NEW");   // resultCode 보유 → incomingWithCode.add("AP")
        dto.setCheckResults(List.of(incoming));

        InspectReport existing = new InspectReport();
        existing.setId(30L); existing.setCreatedBy("orig"); existing.setStatus(DocumentStatus.DRAFT);
        when(reportRepository.findById(30L)).thenReturn(Optional.of(existing));
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> i.getArgument(0));
        // 기존: AP 가 QR 적재(resultCode 있음) — 단, incoming 이 같은 section 을 resultCode 와 함께 재공급
        InspectCheckResult ap = new InspectCheckResult(); ap.setSection("AP"); ap.setResultCode("OK");
        when(checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc(30L)).thenReturn(List.of(ap));
        when(visitLogRepository.findByReportIdOrderBySortOrderAsc(30L)).thenReturn(List.of());

        service.save(dto);

        // qrSections={AP}, incomingWithCode={AP} → toProtect={} → 보호 없음 → 전체 삭제
        verify(checkResultRepository).deleteByReportId(30L);
        verify(checkResultRepository, never()).deleteByReportIdAndSectionIn(anyLong(), anyList());
        verify(checkResultRepository).save(any(InspectCheckResult.class));   // AP 저장(skip 안 함)
    }

    // ===== A4: currentUser — 인증 present → getName()(L294) =====

    @Test
    void save_new_usesAuthenticatedUsername() {
        try {
            // 3-arg ctor → authenticated 토큰(2-arg 는 미인증). currentUser 는 getName() 만 보지만 의미 명확화.
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("alice", "x", java.util.Collections.emptyList()));
            InspectReportDTO dto = new InspectReportDTO();
            dto.setStatus(DocumentStatus.DRAFT);
            when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> {
                InspectReport r = i.getArgument(0); r.setId(80L); return r;
            });
            stubFinalFindById(80L, DocumentStatus.DRAFT);

            service.save(dto);

            ArgumentCaptor<InspectReport> cap = ArgumentCaptor.forClass(InspectReport.class);
            verify(reportRepository).save(cap.capture());
            assertThat(cap.getValue().getCreatedBy()).isEqualTo("alice");
            assertThat(cap.getValue().getUpdatedBy()).isEqualTo("alice");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ===== A5: delete not-found / inspectMonth null → 연도 now() fallback =====

    @Test
    void delete_notFound_throws() {
        when(reportRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(7L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_nullMonth_usesCurrentYearFallback() {
        InspectReport r = new InspectReport();
        r.setId(3L); r.setInspectMonth(null);   // month null → 연도 now() fallback, length>=7 블록 skip
        when(reportRepository.findById(3L)).thenReturn(Optional.of(r));
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(i -> i.getArgument(0));
        when(opsDocumentRepository.findByDocNo(anyString())).thenReturn(Optional.empty());

        service.delete(3L);

        // 연도는 production LocalDate.now() 산출 → 연말 경계 flaky 회피 위해 정규식 매처(연도값 비결합)
        verify(opsDocumentRepository).findByDocNo(matches("INSP-\\d{4}-3"));  // 연도 fallback(month null) 경로
        verify(opsDocumentRepository).findByDocNo("INSP-3");
        verify(opsDocumentRepository, never()).findByDocNo(matches("INSP-\\d{4}-\\d{2}-3"));  // month null → 월포함 docNo 미조회
    }
}
