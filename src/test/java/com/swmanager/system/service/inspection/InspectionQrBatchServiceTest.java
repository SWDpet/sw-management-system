package com.swmanager.system.service.inspection;

import com.swmanager.system.constant.enums.InspectResultCode;
import com.swmanager.system.domain.InspectCheckResult;
import com.swmanager.system.domain.InspectQrBatch;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.dto.inspection.InspectionQrBatchRequest;
import com.swmanager.system.dto.inspection.InspectionQrBatchResponse;
import com.swmanager.system.repository.InspectCheckResultRepository;
import com.swmanager.system.repository.InspectQrBatchRepository;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.SwProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InspectionQrBatchService 단위 테스트.
 * 개발계획서 §1 Step 7 — 12 케이스.
 */
@ExtendWith(MockitoExtension.class)
class InspectionQrBatchServiceTest {

    @Mock InspectQrBatchRepository batchRepository;
    @Mock InspectReportRepository reportRepository;
    @Mock InspectCheckResultRepository checkResultRepository;
    @Mock SwProjectRepository swProjectRepository;

    @InjectMocks InspectionQrBatchService service;

    private SwProject pjt;

    @BeforeEach
    void setUp() {
        pjt = new SwProject();
        pjt.setProjId(17L);
        pjt.setSiteCode("dyg");
        pjt.setSysNmEn("UPIS");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private InspectionQrBatchRequest sampleRequest(String payloadId) {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setS("snapshot/qr1");
        p.setId(payloadId);
        p.setSite("dyg");
        p.setRound("2026-05");
        p.setTs(1778461321L);
        p.setInspector("박욱진");

        Map<String, InspectionQrBatchRequest.Tier> tiers = new LinkedHashMap<>();

        InspectionQrBatchRequest.Tier ap = new InspectionQrBatchRequest.Tier();
        ap.setH("UPIS-AP");
        ap.setOs("WinSvr 2012R2");
        ap.setItems(Arrays.asList(
                Arrays.asList("ap.perf.cpu_pct", "ok", 23.4),
                Arrays.asList("ap.perf.mem_pct", "warn", 81.2),
                Arrays.asList("ap.led.system", "M") // value 누락 케이스
        ));
        tiers.put("ap", ap);

        InspectionQrBatchRequest.Tier db = new InspectionQrBatchRequest.Tier();
        db.setH("UPIS-DB");
        db.setItems(Arrays.asList(
                Arrays.asList("oracle.ts.UPIS_DATA", "warn", 78.4),
                Arrays.asList("db.cable", "M", null)
        ));
        tiers.put("db", db);

        p.setTiers(tiers);
        req.setPayload(p);

        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("abc123");
        h.setRawBytes(1597);
        h.setGzBytes(681);
        h.setB45Chars(1022);
        h.setTotal(1);
        req.setHeader(h);

        return req;
    }

    private void stubReportSaveWithId(long id) {
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(inv -> {
            InspectReport r = inv.getArgument(0);
            r.setId(id);
            return r;
        });
    }

    // ── 1. 정상 흐름 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("1. 정상 payload → report + 5 items INSERT")
    void normalUpload_savesReportAndCheckResults() {
        InspectionQrBatchRequest req = sampleRequest("dyg-2026-05");
        when(batchRepository.findByPayloadId("dyg-2026-05")).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(101L);

        InspectionQrBatchResponse res = service.upload(req, 42L);

        assertThat(res.getReportId()).isEqualTo(101L);
        assertThat(res.isIdempotent()).isFalse();
        assertThat(res.getPjtId()).isEqualTo(17L);
        assertThat(res.getTierCount()).isEqualTo(2);
        assertThat(res.getItemCount()).isEqualTo(5);
        assertThat(res.getManualItems()).isEqualTo(2);
        assertThat(res.getWarnItems()).isEqualTo(2);
        assertThat(res.getReportUrl()).isEqualTo("/document/inspect/101");

        verify(reportRepository, times(1)).save(any(InspectReport.class));
        verify(checkResultRepository, times(5)).save(any(InspectCheckResult.class));
        verify(batchRepository, times(1)).save(any(InspectQrBatch.class));
    }

    // ── 2. 멱등 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("2. 멱등 — 동일 payload_id 두 번째 호출 시 DB 변경 0")
    void idempotent_secondCall_returnsExisting() {
        InspectQrBatch existing = new InspectQrBatch();
        existing.setId(7L);
        existing.setPayloadId("dyg-2026-05");
        existing.setReportId(101L);
        existing.setSiteCode("dyg");
        existing.setHashCheck("ok");
        Map<String, Object> json = new LinkedHashMap<>();
        Map<String, Object> tiers = new LinkedHashMap<>();
        Map<String, Object> ap = new LinkedHashMap<>();
        ap.put("i", List.of(List.of("ap.x", "ok", 1)));
        tiers.put("ap", ap);
        json.put("tiers", tiers);
        existing.setPayloadJson(json);

        when(batchRepository.findByPayloadId("dyg-2026-05")).thenReturn(Optional.of(existing));
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));

        InspectionQrBatchResponse res = service.upload(sampleRequest("dyg-2026-05"), 42L);

        assertThat(res.isIdempotent()).isTrue();
        assertThat(res.getReportId()).isEqualTo(101L);
        assertThat(res.getHashCheck()).isEqualTo("ok");
        assertThat(res.getReportUrl()).isEqualTo("/document/inspect/101");

        verify(reportRepository, never()).save(any());
        verify(checkResultRepository, never()).save(any());
        verify(batchRepository, never()).save(any());
    }

    // ── 3. site 미매핑 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("3. site_not_mapped — 어떤 INSERT 도 발생 X")
    void siteNotMapped_throwsAndNoInserts() {
        InspectionQrBatchRequest req = sampleRequest("unknown-2026-05");
        when(batchRepository.findByPayloadId("unknown-2026-05")).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(req, 42L))
                .isInstanceOf(SiteNotMappedException.class)
                .hasMessageContaining("dyg");

        verify(reportRepository, never()).save(any());
        verify(checkResultRepository, never()).save(any());
        verify(batchRepository, never()).save(any());
    }

    // ── 4. status 매핑 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("4. ok/warn/err/M → NORMAL/PARTIAL/ABNORMAL/NOT_APPLICABLE")
    void statusMapping_allFourCases() {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("test-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier ap = new InspectionQrBatchRequest.Tier();
        ap.setItems(Arrays.asList(
                Arrays.asList("k.ok", "ok", 1),
                Arrays.asList("k.warn", "warn", 2),
                Arrays.asList("k.err", "err", 3),
                Arrays.asList("k.m", "M", null)
        ));
        p.setTiers(Map.of("ap", ap));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("zzzzzz");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<InspectCheckResult> captor = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository, times(4)).save(captor.capture());

        List<InspectCheckResult> rows = captor.getAllValues();
        assertThat(rows.get(0).getResultCode()).isEqualTo(InspectResultCode.NORMAL.name());
        assertThat(rows.get(1).getResultCode()).isEqualTo(InspectResultCode.PARTIAL.name());
        assertThat(rows.get(2).getResultCode()).isEqualTo(InspectResultCode.ABNORMAL.name());
        assertThat(rows.get(3).getResultCode()).isEqualTo(InspectResultCode.NOT_APPLICABLE.name());
    }

    // ── 5. M 항목 remarks 표지 ─────────────────────────────────────────────

    @Test
    @DisplayName("5. M 항목 remarks = '육안 점검 필요 (자동수집 불가)'")
    void manualItem_hasRemarksLabel() {
        InspectionQrBatchRequest req = sampleRequest("test-m");
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<InspectCheckResult> captor = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository, times(5)).save(captor.capture());

        List<InspectCheckResult> manuals = captor.getAllValues().stream()
                .filter(r -> InspectResultCode.NOT_APPLICABLE.name().equals(r.getResultCode()))
                .toList();
        assertThat(manuals).hasSize(2);
        assertThat(manuals.get(0).getRemarks()).contains("육안 점검 필요");
    }

    // ── 6. 알 수 없는 status ───────────────────────────────────────────────

    @Test
    @DisplayName("6. 알 수 없는 status → NORMAL fallback + remarks 표지")
    void unknownStatus_fallbackNormalWithRemarks() {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("unk-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier t = new InspectionQrBatchRequest.Tier();
        t.setItems(List.of(List.of("k.x", "weird_status", 1)));
        p.setTiers(Map.of("ap", t));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("z");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<InspectCheckResult> captor = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository).save(captor.capture());
        InspectCheckResult row = captor.getValue();
        assertThat(row.getResultCode()).isEqualTo(InspectResultCode.NORMAL.name());
        assertThat(row.getRemarks()).contains("알 수 없는 상태").contains("weird_status");
    }

    // ── 7. result_text 길이 500 초과 절단 ──────────────────────────────────

    @Test
    @DisplayName("7. result_text 길이 500 초과 → 절단 + remarks 에 (truncated)")
    void resultTextTruncation_atMaxLength() {
        String longValue = "x".repeat(700);
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("long-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier t = new InspectionQrBatchRequest.Tier();
        t.setItems(List.of(List.of("k.long", "ok", longValue)));
        p.setTiers(Map.of("ap", t));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("z");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<InspectCheckResult> captor = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository).save(captor.capture());
        InspectCheckResult row = captor.getValue();
        assertThat(row.getResultText()).hasSize(500);
        assertThat(row.getRemarks()).contains("(truncated)");
    }

    // ── 8. 빈 tier 항목 ───────────────────────────────────────────────────

    @Test
    @DisplayName("8. tier 의 i 가 비어있으면 0 items")
    void emptyTierItems_noCheckResults() {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("empty-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier t = new InspectionQrBatchRequest.Tier();
        t.setItems(List.of());
        p.setTiers(Map.of("ap", t));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("z");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        InspectionQrBatchResponse res = service.upload(req, 1L);
        assertThat(res.getItemCount()).isZero();
        verify(checkResultRepository, never()).save(any());
    }

    // ── 9. payload.ts null 처리 ───────────────────────────────────────────

    @Test
    @DisplayName("9. payload.ts / inspector null 도 정상 처리")
    void nullableOptionalFields_handledGracefully() {
        InspectionQrBatchRequest req = sampleRequest("nullable-2026-05");
        req.getPayload().setTs(null);
        req.getPayload().setInspector(null);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(2L);

        ArgumentCaptor<InspectQrBatch> captor = ArgumentCaptor.forClass(InspectQrBatch.class);
        service.upload(req, 1L);
        verify(batchRepository).save(captor.capture());
        InspectQrBatch b = captor.getValue();
        assertThat(b.getPayloadTs()).isNull();
        assertThat(b.getSourceInspector()).isNull();
    }

    // ── 10. hash_check ─────────────────────────────────────────────────────

    @Test
    @DisplayName("10. hash 불일치 → hash_check=warn, 그래도 INSERT 진행")
    void hashMismatch_warnOnlyNotReject() {
        InspectionQrBatchRequest req = sampleRequest("hash-2026-05");
        req.getHeader().setHash("ffffff"); // 임의 — 실제 sha 와 다름

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(3L);

        ArgumentCaptor<InspectQrBatch> captor = ArgumentCaptor.forClass(InspectQrBatch.class);
        InspectionQrBatchResponse res = service.upload(req, 1L);
        verify(batchRepository).save(captor.capture());
        assertThat(captor.getValue().getHashCheck()).isIn("warn", "skip");
        assertThat(res.getHashCheck()).isIn("warn", "skip");
        assertThat(res.getReportId()).isEqualTo(3L);
    }

    // ── 11. report 필드 매핑 ──────────────────────────────────────────────

    @Test
    @DisplayName("11. inspect_report — source=auto-qr, batch_id=payload.id, sys_type=pjt.sysNmEn")
    void reportFields_filledFromPayloadAndPjt() {
        InspectionQrBatchRequest req = sampleRequest("dyg-2026-05");
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(50L);

        ArgumentCaptor<InspectReport> captor = ArgumentCaptor.forClass(InspectReport.class);
        service.upload(req, 1L);
        verify(reportRepository).save(captor.capture());
        InspectReport r = captor.getValue();
        assertThat(r.getSource()).isEqualTo("auto-qr");
        assertThat(r.getBatchId()).isEqualTo("dyg-2026-05");
        assertThat(r.getPjtId()).isEqualTo(17L);
        assertThat(r.getSysType()).isEqualTo("UPIS");
        assertThat(r.getInspectMonth()).isEqualTo("2026-05");
        assertThat(r.getDocTitle()).contains("UPIS").contains("2026-05");
    }

    // ── 12. batch payload_json 보존 ───────────────────────────────────────

    @Test
    @DisplayName("12. inspect_qr_batch.payload_json 에 site/round/tiers 모두 보존")
    void batchPayloadJson_preservesOriginal() {
        InspectionQrBatchRequest req = sampleRequest("dyg-2026-05");
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(70L);

        ArgumentCaptor<InspectQrBatch> captor = ArgumentCaptor.forClass(InspectQrBatch.class);
        service.upload(req, 1L);
        verify(batchRepository).save(captor.capture());
        Map<String, Object> json = captor.getValue().getPayloadJson();
        assertThat(json).containsEntry("site", "dyg").containsEntry("round", "2026-05");
        assertThat(json.get("tiers")).isInstanceOf(Map.class);
    }
}
