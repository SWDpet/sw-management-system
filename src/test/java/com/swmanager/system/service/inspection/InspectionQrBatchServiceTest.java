package com.swmanager.system.service.inspection;

import com.swmanager.system.constant.enums.InspectResultCode;
import com.swmanager.system.domain.InspectCheckResult;
import com.swmanager.system.domain.InspectQrBatch;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.dto.inspection.InspectionQrBatchRequest;
import com.swmanager.system.dto.inspection.InspectionQrBatchResponse;
import com.swmanager.system.repository.InspectCheckResultRepository;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    @Mock InspectMetricSnapshotRepository metricSnapshotRepository;

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

    // ── 7-b. context-format 경로(truncated=false) 500 초과 → 2차 절단 시 remarks (truncated) (§8-1) ──

    @Test
    @DisplayName("7-b. formatValueWithContext 가 600자 truncated=false 반환 → 서비스 절단 + (truncated) 표기")
    void resultTextTruncation_contextPath_flagReflected() {
        String longExport = "e".repeat(600);   // db.oracle.export_last → String.valueOf(last_export), truncated=false
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("longctx-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier t = new InspectionQrBatchRequest.Tier();
        t.setItems(List.of(List.of("db.oracle.export_last", "ok", Map.of("last_export", longExport))));
        p.setTiers(Map.of("dbms", t));
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
        assertThat(row.getRemarks()).contains("(truncated)");   // §8-1: 2차 절단도 플래그 반영
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

    // ── 13. metric snapshot — AP perf cpu/mem upsert ──────────────────────────

    @Test
    @DisplayName("13. AP tier perf cpu/mem → upsertIgnore(AP, host, cpu, mem, collectedAt=ts)")
    void metricSnapshot_apPerfMetrics_upserted() {
        InspectionQrBatchRequest req = sampleRequest("metric-2026-05");  // ap.perf.cpu_pct=23.4, mem_pct=81.2
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<OffsetDateTime> tsCap = ArgumentCaptor.forClass(OffsetDateTime.class);
        service.upload(req, 1L);

        // AP 만 perf 보유(DB tier 는 perf 없음) → 정확히 1회, AP 값으로 upsert
        verify(metricSnapshotRepository, times(1)).upsertIgnore(
                eq(17L), eq("AP"), eq("UPIS-AP"), isNull(),
                tsCap.capture(),
                eq(BigDecimal.valueOf(23.4)),   // cpu_pct
                eq(BigDecimal.valueOf(81.2)),   // mem_pct
                isNull(),                       // disk 없음
                anyString());                   // rawJson
        // collectedAt = payload.ts(unix seconds) 기준
        assertThat(tsCap.getValue())
                .isEqualTo(OffsetDateTime.ofInstant(Instant.ofEpochSecond(1778461321L), ZoneOffset.UTC));
    }

    @Test
    @DisplayName("14. perf 데이터 없으면 metric upsert 미발생")
    void metricSnapshot_noPerfData_skipped() {
        // tier 항목이 perf 키가 아님 → cpu/mem/disk 전부 null → role skip
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("noperf-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier ap = new InspectionQrBatchRequest.Tier();
        ap.setH("H1");
        ap.setItems(List.of(List.of("ap.cable", "ok", 5)));   // perf 키 아님
        p.setTiers(Map.of("ap", ap));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("z");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        service.upload(req, 1L);

        verify(metricSnapshotRepository, never()).upsertIgnore(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ── 15. buildBatch 전 필드 매핑 ───────────────────────────────────────────

    @Test
    @DisplayName("15. inspect_qr_batch 전 필드(payloadId/site/round/ts/inspector/hash/bytes/uploadedBy)")
    void batch_allFields_mappedFromPayloadHeaderUser() {
        InspectionQrBatchRequest req = sampleRequest("dyg-2026-05");
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(55L);

        ArgumentCaptor<InspectQrBatch> cap = ArgumentCaptor.forClass(InspectQrBatch.class);
        service.upload(req, 42L);
        verify(batchRepository).save(cap.capture());
        InspectQrBatch b = cap.getValue();

        assertThat(b.getPayloadId()).isEqualTo("dyg-2026-05");
        assertThat(b.getReportId()).isEqualTo(55L);
        assertThat(b.getSiteCode()).isEqualTo("dyg");
        assertThat(b.getInspectRound()).isEqualTo("2026-05");
        assertThat(b.getPayloadTs()).isEqualTo(1778461321L);
        assertThat(b.getSourceInspector()).isEqualTo("박욱진");
        assertThat(b.getHeaderHash()).isEqualTo("abc123");
        assertThat(b.getRawBytes()).isEqualTo(1597);
        assertThat(b.getGzBytes()).isEqualTo(681);
        assertThat(b.getUploadedBy()).isEqualTo(42L);
    }

    // ── 16. saveCheckResults 행 필드(reportId/section/itemName/sortOrder) ──────

    @Test
    @DisplayName("16. inspect_check_result — reportId/section/itemName/sortOrder 채움")
    void checkResult_fieldsPopulated() {
        InspectionQrBatchRequest req = sampleRequest("dyg-2026-05");
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(101L);

        ArgumentCaptor<InspectCheckResult> cap = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository, times(5)).save(cap.capture());

        InspectCheckResult cpu = cap.getAllValues().stream()
                .filter(r -> "ap.perf.cpu_pct".equals(r.getItemName())).findFirst().orElseThrow();
        assertThat(cpu.getReportId()).isEqualTo(101L);
        assertThat(cpu.getSection()).isEqualTo("AP");        // resolveSection 기본
        assertThat(cpu.getSortOrder()).isPositive();          // setSortOrder 호출 검증(제거 시 0)

        InspectCheckResult dbCable = cap.getAllValues().stream()
                .filter(r -> "db.cable".equals(r.getItemName())).findFirst().orElseThrow();
        assertThat(dbCable.getSection()).isEqualTo("DB");     // db. prefix → DB
    }

    // ── 17. 멱등 응답 카운트(toIdempotentResponse) ────────────────────────────

    @Test
    @DisplayName("17. 멱등 응답 — payloadJson 에서 tier/item/manual/warn 카운트")
    void idempotentResponse_countsFromPayloadJson() {
        InspectQrBatch existing = new InspectQrBatch();
        existing.setId(7L);
        existing.setPayloadId("dyg-2026-05");
        existing.setReportId(101L);
        existing.setSiteCode("dyg");
        existing.setHashCheck("ok");

        Map<String, Object> apItems = new LinkedHashMap<>();
        apItems.put("i", List.of(
                List.of("a", "ok", 1),
                List.of("b", "M"),       // size 2 → 카운트 + manual
                List.of("c", "warn", 3),
                List.of("x")));          // size 1 → 미카운트
        Map<String, Object> dbItems = new LinkedHashMap<>();
        dbItems.put("i", List.of(List.of("d", "ok", 1)));
        Map<String, Object> tiers = new LinkedHashMap<>();
        tiers.put("ap", apItems);
        tiers.put("db", dbItems);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("tiers", tiers);
        existing.setPayloadJson(json);

        when(batchRepository.findByPayloadId("dyg-2026-05")).thenReturn(Optional.of(existing));
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));

        InspectionQrBatchResponse res = service.upload(sampleRequest("dyg-2026-05"), 1L);

        assertThat(res.isIdempotent()).isTrue();
        assertThat(res.getPjtId()).isEqualTo(17L);
        assertThat(res.getTierCount()).isEqualTo(2);
        assertThat(res.getItemCount()).isEqualTo(4);   // a,b,c,d (x 제외)
        assertThat(res.getManualItems()).isEqualTo(1); // b
        assertThat(res.getWarnItems()).isEqualTo(1);   // c
        assertThat(res.getReportUrl()).isEqualTo("/document/inspect/101");
    }

    // ── 15. merge — 동일 (pjt, month) 기존 report 존재 시 머지 ───────────────
    @Test
    @DisplayName("15. 기존 manual report 존재 → merge(batchId/source 기록 + 자동수집 섹션만 삭제)")
    void upload_mergesIntoExistingReport() {
        InspectionQrBatchRequest req = sampleRequest("dyg-2026-05");
        InspectReport existing = new InspectReport();
        existing.setId(500L);
        existing.setPjtId(17L);
        existing.setInspectMonth("2026-05");   // batchId null → merge 시 기록 대상

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        when(reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull(17L, "2026-05"))
                .thenReturn(Optional.of(existing));
        when(reportRepository.save(any(InspectReport.class))).thenAnswer(inv -> inv.getArgument(0));

        InspectionQrBatchResponse res = service.upload(req, 99L);

        assertThat(res.getReportId()).isEqualTo(500L);                 // 기존 report 재사용
        assertThat(existing.getBatchId()).isEqualTo("dyg-2026-05");
        assertThat(existing.getSource()).isEqualTo("auto-qr-merged");
        assertThat(existing.getInspUserId()).isEqualTo(99L);           // null 이던 점검자 주입
        verify(checkResultRepository).deleteByReportIdAndSectionIn(
                eq(500L), eq(List.of("AP", "DB", "DBMS", "GIS")));      // 자동수집 섹션만 삭제
    }

    // ── 16. db.os.disk mounts → DB_USAGE / ap.os.disk_summary drives → AP_USAGE ─
    @Test
    @DisplayName("16. disk 맵 → DB_USAGE / AP_USAGE 사용량 행 생성")
    void usageRows_generatedFromDiskMaps() {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("usage-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier db = new InspectionQrBatchRequest.Tier();
        db.setItems(List.of(List.of("db.os.disk", "ok", Map.of("/", Map.of("p", 52)))));
        InspectionQrBatchRequest.Tier ap = new InspectionQrBatchRequest.Tier();
        ap.setItems(List.of(List.of("ap.os.disk_summary", "ok", Map.of("c", Map.of("t", 5, "f", 2)))));
        p.setTiers(Map.of("db", db, "ap", ap));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("z");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<InspectCheckResult> cap = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository, org.mockito.Mockito.atLeastOnce()).save(cap.capture());
        List<InspectCheckResult> rows = cap.getAllValues();
        // DB_USAGE: mount "/" → 사용률 "52%"
        assertThat(rows).anySatisfy(r -> {
            assertThat(r.getSection()).isEqualTo("DB_USAGE");
            assertThat(r.getItemName()).isEqualTo("/");
            assertThat(r.getResultText()).isEqualTo("52%");
        });
        // AP_USAGE: drive c → "Disk C:" 라벨, "5GB / 2GB 여유"
        assertThat(rows).anySatisfy(r -> {
            assertThat(r.getSection()).isEqualTo("AP_USAGE");
            assertThat(r.getItemName()).isEqualTo("Disk C:");
            assertThat(r.getResultText()).isEqualTo("5GB / 2GB 여유");
        });
    }

    // ── 17. siteCode 직접 매핑 실패 → alias 폴백 ──────────────────────────────
    @Test
    @DisplayName("17. findBySiteCode empty → findFirstBySiteCodeAlias 폴백")
    void siteCode_aliasFallback() {
        InspectionQrBatchRequest req = sampleRequest("alias-2026-05");
        req.getPayload().setSite("oldcode");
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("oldcode")).thenReturn(Optional.empty());
        when(swProjectRepository.findFirstBySiteCodeAlias("oldcode")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        InspectionQrBatchResponse res = service.upload(req, 1L);
        assertThat(res.getPjtId()).isEqualTo(17L);
        verify(swProjectRepository).findFirstBySiteCodeAlias("oldcode");
    }

    // ── 18. ts null → snapshot collectedAt=now() 로 upsert ────────────────────
    @Test
    @DisplayName("18. payload.ts null 이어도 AP perf → snapshot upsert(collectedAt=now)")
    void metricSnapshot_tsNull_usesNow() {
        InspectionQrBatchRequest req = sampleRequest("tsnull-2026-05");
        req.getPayload().setTs(null);
        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<OffsetDateTime> tsCap = ArgumentCaptor.forClass(OffsetDateTime.class);
        service.upload(req, 1L);
        verify(metricSnapshotRepository, times(1)).upsertIgnore(
                eq(17L), eq("AP"), eq("UPIS-AP"), isNull(),
                tsCap.capture(),
                eq(BigDecimal.valueOf(23.4)), eq(BigDecimal.valueOf(81.2)), isNull(), anyString());
        // ts=null → collectedAt=now() 사용 검증 (payload.ts 기반 2026-05-11 과거값이 아니라 현재 시각)
        assertThat(tsCap.getValue()).isAfter(OffsetDateTime.now().minusMinutes(5));
    }

    // ── 19. public findExisting — 멱등 응답 직접 반환(upload 내부경로와 별개) ────
    @Test
    @DisplayName("19. findExisting(payloadId) → 멱등 응답(toIdempotentResponse) 반환")
    void findExisting_found_returnsIdempotent() {
        InspectQrBatch batch = new InspectQrBatch();
        batch.setId(7L);
        batch.setPayloadId("dyg-2026-05");
        batch.setReportId(101L);
        batch.setSiteCode("dyg");
        Map<String, Object> ap = new LinkedHashMap<>();
        // item a(3요소 ok)·b(2요소 manual) — manual 항목은 값 필드 없는 2요소가 정상 계약(test-17 동일)
        ap.put("i", List.of(List.of("a", "ok", 1), List.of("b", "M")));
        Map<String, Object> tiers = new LinkedHashMap<>();
        tiers.put("ap", ap);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("tiers", tiers);
        batch.setPayloadJson(json);

        when(batchRepository.findByPayloadId("dyg-2026-05")).thenReturn(Optional.of(batch));
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));   // present → alias 미호출

        Optional<InspectionQrBatchResponse> res = service.findExisting("dyg-2026-05");

        assertThat(res).isPresent();
        InspectionQrBatchResponse r = res.get();
        assertThat(r.isIdempotent()).isTrue();
        assertThat(r.getPjtId()).isEqualTo(17L);
        assertThat(r.getReportId()).isEqualTo(101L);
        assertThat(r.getReportUrl()).isEqualTo("/document/inspect/101");
        assertThat(r.getTierCount()).isEqualTo(1);
        assertThat(r.getItemCount()).isEqualTo(2);     // a, b
        assertThat(r.getManualItems()).isEqualTo(1);   // b
    }

    // ── 20. usage mount 값이 non-Map → String.valueOf(mval)+"%" (else 분기) ─────
    @Test
    @DisplayName("20. db.os.disk mount 값 비-Map(75) → DB_USAGE '75%'")
    void usageRows_nonMapMountValue_plainPercent() {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("usage2-2026-05");
        p.setSite("dyg");
        p.setRound("2026-05");
        InspectionQrBatchRequest.Tier db = new InspectionQrBatchRequest.Tier();
        db.setItems(List.of(List.of("db.os.disk", "ok", Map.of("/", 75))));   // 75=Integer(비-Map) → else 분기
        p.setTiers(Map.of("db", db));
        req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        h.setHash("z");
        req.setHeader(h);

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(1L);

        ArgumentCaptor<InspectCheckResult> cap = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository, org.mockito.Mockito.atLeastOnce()).save(cap.capture());
        // mount "/" 값이 Map 아님 → "75%" (Map 분기였다면 p 키 추출이라 다른 경로).
        // 정확히 1개 DB_USAGE "/" 행(중복행 회귀까지 차단).
        assertThat(cap.getAllValues())
                .filteredOn(r -> "DB_USAGE".equals(r.getSection()) && "/".equals(r.getItemName()))
                .singleElement()
                .satisfies(r -> assertThat(r.getResultText()).isEqualTo("75%"));
    }

    // ── 21. buildReport 신규 보고서 감사필드(beyond-A 뮤테이션 강화) ──────────────
    @Test
    @DisplayName("21. buildReport — inspUserId/createdBy/updatedBy 박제(L175~177)")
    void buildReport_setsAuditFields() {
        InspectionQrBatchRequest req = sampleRequest("audit-2026-05");
        when(batchRepository.findByPayloadId("audit-2026-05")).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(210L);

        service.upload(req, 42L);

        ArgumentCaptor<InspectReport> cap = ArgumentCaptor.forClass(InspectReport.class);
        verify(reportRepository).save(cap.capture());
        InspectReport r = cap.getValue();
        assertThat(r.getInspUserId()).isEqualTo(42L);    // L175
        assertThat(r.getCreatedBy()).isEqualTo("42");     // L176
        assertThat(r.getUpdatedBy()).isEqualTo("42");     // L177
        assertThat(r.getSource()).isEqualTo("auto-qr");
        assertThat(r.getBatchId()).isEqualTo("audit-2026-05");
    }

    // ── 22. verifyHash 직접 호출 — null/blank→skip, 불일치→warn(L384·395 + sha1Hex L419) ──
    @Test
    @DisplayName("22. verifyHash null/blank→skip, 불일치→warn")
    void verifyHash_skipAndWarn() {
        InspectionQrBatchRequest.Payload p = sampleRequest("vh-2026-05").getPayload();
        assertThat(service.verifyHash(p, null)).isEqualTo("skip");      // L384 null
        assertThat(service.verifyHash(p, "   ")).isEqualTo("skip");     // L384 blank
        // 실제 계산해시(6-hex)는 "zzzzzz"와 절대 불일치 → "warn". sha1Hex 가 ""(mutant)면 substring 예외→"skip"이라 kill.
        assertThat(service.verifyHash(p, "zzzzzz")).isEqualTo("warn");  // L395 + sha1Hex L419
    }

    // ── 23. DB_USAGE/AP_USAGE 행 reportId + sortOrder 박제(L251·255·275·279) ──────
    @Test
    @DisplayName("23. usage 행 reportId+sortOrder 증가")
    void usageRows_reportIdAndSortOrder() {
        InspectionQrBatchRequest req = new InspectionQrBatchRequest();
        InspectionQrBatchRequest.Payload p = new InspectionQrBatchRequest.Payload();
        p.setId("usort-2026-05"); p.setSite("dyg"); p.setRound("2026-05");
        InspectionQrBatchRequest.Tier db = new InspectionQrBatchRequest.Tier();
        Map<String, Object> mounts = new LinkedHashMap<>();
        mounts.put("/", Map.of("p", 55));
        mounts.put("/backup", Map.of("p", 70));
        db.setItems(List.of(List.of("db.os.disk", "ok", mounts)));
        InspectionQrBatchRequest.Tier ap = new InspectionQrBatchRequest.Tier();
        Map<String, Object> drives = new LinkedHashMap<>();
        drives.put("c", Map.of("p", 60));
        drives.put("d", Map.of("p", 40));   // 2번째 드라이브 → diskSort 2,3 증가(L279 Increments kill)
        ap.setItems(List.of(List.of("ap.os.disk_summary", "ok", drives)));
        Map<String, InspectionQrBatchRequest.Tier> tiers = new LinkedHashMap<>();
        tiers.put("db", db); tiers.put("ap", ap);
        p.setTiers(tiers); req.setPayload(p);
        InspectionQrBatchRequest.Header h = new InspectionQrBatchRequest.Header();
        req.setHeader(h);   // hash null → verifyHash skip

        when(batchRepository.findByPayloadId(any())).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        stubReportSaveWithId(220L);

        ArgumentCaptor<InspectCheckResult> cap = ArgumentCaptor.forClass(InspectCheckResult.class);
        service.upload(req, 1L);
        verify(checkResultRepository, org.mockito.Mockito.atLeastOnce()).save(cap.capture());

        var dbUsage = cap.getAllValues().stream().filter(r -> "DB_USAGE".equals(r.getSection())).toList();
        assertThat(dbUsage).allSatisfy(r -> assertThat(r.getReportId()).isEqualTo(220L));     // L251
        assertThat(dbUsage).extracting(InspectCheckResult::getSortOrder).containsExactly(0, 1); // L255 usageSort++
        var apUsage = cap.getAllValues().stream().filter(r -> "AP_USAGE".equals(r.getSection())).toList();
        assertThat(apUsage).allSatisfy(r -> assertThat(r.getReportId()).isEqualTo(220L));     // L275
        assertThat(apUsage).extracting(InspectCheckResult::getSortOrder).containsExactly(2, 3); // L279 diskSort 2,3 증가
    }

    // ── 24. upload 기존 회차 merge 경로 — updatedBy 갱신(L117) ────────────────────
    @Test
    @DisplayName("24. merge 경로 — 기존 보고서 updatedBy 갱신(L117)")
    void upload_mergeExistingReport_updatesUpdatedBy() {
        InspectionQrBatchRequest req = sampleRequest("merge-2026-05");
        when(batchRepository.findByPayloadId("merge-2026-05")).thenReturn(Optional.empty());
        when(swProjectRepository.findBySiteCode("dyg")).thenReturn(Optional.of(pjt));
        InspectReport existing = new InspectReport();
        existing.setId(500L);
        existing.setBatchId("prior");          // 이미 batchId 있음 → 보존
        existing.setUpdatedBy("old");
        when(reportRepository.findByPjtIdAndInspectMonthAndDeletedAtIsNull(17L, "2026-05"))
                .thenReturn(Optional.of(existing));

        service.upload(req, 99L);

        // merge 경로에서 updatedBy 를 userId 로 갱신(L117). userId 99 → "99".
        assertThat(existing.getUpdatedBy()).isEqualTo("99");
        assertThat(existing.getBatchId()).isEqualTo("prior");   // 기존 batchId 보존(이미 머지된 회차)
        verify(reportRepository).save(existing);
    }

    // ── 25. 멱등 — site_code 직접 미매핑, alias 폴백 경로(toIdempotentResponse L425) ──
    @Test
    @DisplayName("25. 멱등 alias 폴백 — findBySiteCode 빈값 → findFirstBySiteCodeAlias")
    void idempotent_aliasFallback_resolvesPjt() {
        InspectQrBatch existing = new InspectQrBatch();
        existing.setId(8L); existing.setPayloadId("alias-2026-05");
        existing.setReportId(303L); existing.setSiteCode("aliasSite"); existing.setHashCheck("ok");

        when(batchRepository.findByPayloadId("alias-2026-05")).thenReturn(Optional.of(existing));
        when(swProjectRepository.findBySiteCode("aliasSite")).thenReturn(Optional.empty());   // 직접 미매핑
        SwProject aliasPjt = new SwProject(); aliasPjt.setProjId(77L);
        when(swProjectRepository.findFirstBySiteCodeAlias("aliasSite")).thenReturn(Optional.of(aliasPjt));  // alias 폴백

        InspectionQrBatchResponse res = service.upload(sampleRequest2("alias-2026-05", "aliasSite"), 1L);

        assertThat(res.isIdempotent()).isTrue();
        assertThat(res.getPjtId()).isEqualTo(77L);    // alias 폴백으로 해소(L425)
    }

    /** site/payloadId 를 지정하는 sampleRequest 변형(멱등 alias 테스트용). */
    private InspectionQrBatchRequest sampleRequest2(String payloadId, String site) {
        InspectionQrBatchRequest req = sampleRequest(payloadId);
        req.getPayload().setSite(site);
        return req;
    }
}
