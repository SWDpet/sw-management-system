package com.swmanager.system.service.inspection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

/**
 * 점검 QR batch 업로드 서비스.
 *
 * 기획서: docs/product-specs/inspection-qr-batch.md v1
 * 개발계획서: docs/exec-plans/inspection-qr-batch.md v1
 *
 * 흐름:
 *  A. 멱등 조회 (payload_id UNIQUE)
 *  B. site_code → pjt_id 매핑 (SwProjectRepository#findBySiteCode)
 *  C. inspect_report INSERT (source='auto-qr', batch_id=payload.id)
 *  D. inspect_check_result rows INSERT (tier 별 metric → row)
 *  E. header.hash 보조검증 (warn-only, NFR-3)
 *  F. inspect_qr_batch INSERT (원본 + report_id 연결)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionQrBatchService {

    private final InspectQrBatchRepository batchRepository;
    private final InspectReportRepository reportRepository;
    private final InspectCheckResultRepository checkResultRepository;
    private final SwProjectRepository swProjectRepository;
    private final InspectMetricSnapshotRepository metricSnapshotRepository;

    /**
     * 본 서비스 안에서 hash 보조검증 시 사용. PoC encode.py 의 직렬화와 가능한 한 동일하게
     * (key 순서 유지, ASCII 비강제). 단 NFR-3 의 warn-only 정책 — 불일치 시도 INSERT 진행.
     */
    private static final ObjectMapper HASH_MAPPER = JsonMapper.builder()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    /**
     * 멱등 조회 — 트랜잭션 외부에서 fast-path 체크.
     */
    @Transactional(readOnly = true)
    public Optional<InspectionQrBatchResponse> findExisting(String payloadId) {
        return batchRepository.findByPayloadId(payloadId).map(this::toIdempotentResponse);
    }

    @Transactional
    public InspectionQrBatchResponse upload(InspectionQrBatchRequest req, Long userId) {
        InspectionQrBatchRequest.Payload payload = req.getPayload();

        // A. 멱등
        Optional<InspectQrBatch> existing = batchRepository.findByPayloadId(payload.getId());
        if (existing.isPresent()) {
            log.info("inspection-qr-batch idempotent payload_id={} report_id={}",
                    payload.getId(), existing.get().getReportId());
            return toIdempotentResponse(existing.get());
        }

        // B. site → pjt (신코드 우선, 구 site_code 는 별칭으로 폴백)
        SwProject pjt = swProjectRepository.findBySiteCode(payload.getSite())
                .or(() -> swProjectRepository.findFirstBySiteCodeAlias(payload.getSite()))
                .orElseThrow(() -> new SiteNotMappedException(payload.getSite()));

        // C. inspect_report — 동일 (pjt, month) 의 기존 manual 회차가 있으면 merge,
        //    없으면 신규 생성. (inspection-report-d-v5 wizard: 점검자가 STEP 1 에서
        //    회차를 먼저 만들어두고 STEP 2 에서 QR 적재를 기다리는 흐름 지원.)
        InspectReport report;
        Optional<InspectReport> existingReport = reportRepository
                .findByPjtIdAndInspectMonthAndDeletedAtIsNull(pjt.getProjId(), payload.getRound());
        if (existingReport.isPresent()) {
            report = existingReport.get();
            // batch_id 가 비어있을 때만 기록 (이미 다른 QR 가 머지된 회차면 보존)
            if (report.getBatchId() == null) {
                report.setBatchId(payload.getId());
                report.setSource("auto-qr-merged");
            }
            if (report.getInspUserId() == null && userId != null) {
                report.setInspUserId(userId);
            }
            report.setUpdatedBy(userId != null ? String.valueOf(userId) : report.getUpdatedBy());
            reportRepository.save(report);
            // 기존 자동수집(AP/DB/DBMS/GIS) 결과만 제거 후 재적재. 수동 입력(APP, *_USAGE, *_ETC) 은 보존.
            checkResultRepository.deleteByReportIdAndSectionIn(
                    report.getId(), java.util.List.of("AP", "DB", "DBMS", "GIS"));
            log.info("inspection-qr-batch merged into existing report id={} (pjt={}, month={})",
                    report.getId(), pjt.getProjId(), payload.getRound());
        } else {
            report = buildReport(payload, pjt, userId);
            reportRepository.save(report);
        }

        // D. inspect_check_result rows
        ItemStats stats = saveCheckResults(report.getId(), payload.getTiers());

        // D'. v6 — inspect_metric_snapshot 누적 (AP/DB 만, warn-only)
        try {
            saveMetricSnapshot(payload, pjt.getProjId());
        } catch (Exception e) {
            log.warn("inspect-metric-snapshot 적재 실패 — warn-only (DB팀 D-5): pjt={} payload={} err={}",
                    pjt.getProjId(), payload.getId(), e.getMessage());
        }

        // E. header hash 보조검증 (warn-only)
        String hashCheck = verifyHash(payload, req.getHeader().getHash());

        // F. inspect_qr_batch
        InspectQrBatch batch = buildBatch(payload, req.getHeader(), report.getId(), userId, hashCheck);
        batchRepository.save(batch);

        log.info("inspection-qr-batch ok payload_id={} report_id={} items={} manual={} warn={} hash={}",
                payload.getId(), report.getId(), stats.total, stats.manual, stats.warn, hashCheck);

        return InspectionQrBatchResponse.builder()
                .reportId(report.getId())
                .batchId(payload.getId())
                .idempotent(false)
                .pjtId(pjt.getProjId())
                .tierCount(payload.getTiers().size())
                .itemCount(stats.total)
                .manualItems(stats.manual)
                .warnItems(stats.warn)
                .hashCheck(hashCheck)
                .reportUrl("/document/inspect/" + report.getId())
                .build();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private InspectReport buildReport(InspectionQrBatchRequest.Payload payload, SwProject pjt, Long userId) {
        InspectReport report = new InspectReport();
        report.setPjtId(pjt.getProjId());
        report.setInspectMonth(payload.getRound());
        report.setSysType(pjt.getSysNmEn());
        String sysLabel = pjt.getSysNmEn() != null ? pjt.getSysNmEn() : payload.getSite().toUpperCase(Locale.ROOT);
        report.setDocTitle("[자동수집] " + sysLabel + " " + payload.getRound() + " 점검내역서");
        report.setSource("auto-qr");
        report.setBatchId(payload.getId());
        report.setInspUserId(userId);
        report.setCreatedBy(userId != null ? String.valueOf(userId) : null);
        report.setUpdatedBy(userId != null ? String.valueOf(userId) : null);
        return report;
    }

    private ItemStats saveCheckResults(Long reportId, Map<String, InspectionQrBatchRequest.Tier> tiers) {
        ItemStats stats = new ItemStats();
        Map<String, Integer> fallbackCounters = new java.util.LinkedHashMap<>();

        for (String tierKey : new ArrayList<>(tiers.keySet())) {
            InspectionQrBatchRequest.Tier tier = tiers.get(tierKey);
            if (tier == null || tier.getItems() == null) continue;
            for (List<Object> metric : tier.getItems()) {
                if (metric == null || metric.size() < 2) continue;
                String key = Objects.toString(metric.get(0), null);
                String status = Objects.toString(metric.get(1), null);
                Object value = metric.size() >= 3 ? metric.get(2) : null;

                String section = InspectQrMetricSupport.resolveSection(key);
                Integer mapped = InspectQrMetricSupport.manifestSort(key);
                int sortOrder;
                if (mapped != null) {
                    sortOrder = mapped;
                } else {
                    sortOrder = fallbackCounters.merge(section, 100, (a, b) -> a);
                    fallbackCounters.put(section, sortOrder + 1);
                    log.warn("manifest 매핑 없음 — fallback sort: section={} key={} sort={}", section, key, sortOrder);
                }

                InspectCheckResult row = new InspectCheckResult();
                row.setReportId(reportId);
                row.setSection(section);
                row.setItemName(key);
                InspectResultCode code = InspectResultCode.fromPoCStatus(status);
                row.setResultCode(code != null ? code.name() : InspectResultCode.NORMAL.name());

                InspectQrMetricSupport.ResultText resultText = InspectQrMetricSupport.formatValueWithContext(key, value);
                String rtText = resultText.text();
                boolean actuallyTruncated = resultText.truncated();
                if (rtText != null && rtText.length() > InspectQrMetricSupport.RESULT_TEXT_MAX) {
                    rtText = rtText.substring(0, InspectQrMetricSupport.RESULT_TEXT_MAX);
                    actuallyTruncated = true;   // 2차 절단도 (truncated) remarks 에 반영 (§8-1)
                }
                row.setResultText(rtText);
                row.setRemarks(InspectQrMetricSupport.buildRemarks(status, code, actuallyTruncated));
                row.setSortOrder(sortOrder);

                checkResultRepository.save(row);
                stats.total++;
                if ("M".equalsIgnoreCase(status)) stats.manual++;
                else if ("warn".equalsIgnoreCase(status)) stats.warn++;

                // db.os.disk: mounts 데이터 → DB_USAGE 행 생성
                if ("db.os.disk".equals(key) && value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mounts = (Map<String, Object>) value;
                    Map<String, String> mountToCategory = Map.of(
                        "/", "/", "/backup", "/backup", "/oracle", "/oracle",
                        "/oradata", "/oradata", "/archive", "/archive");
                    int usageSort = 0;
                    for (var entry : mounts.entrySet()) {
                        String mount = entry.getKey();
                        String cat = mountToCategory.get(mount);
                        if (cat == null) continue;
                        Object mval = entry.getValue();
                        String usageText = "";
                        if (mval instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> md = (Map<String, Object>) mval;
                            Object p = md.get("p");
                            usageText = p != null ? p + "%" : "";
                        } else {
                            usageText = String.valueOf(mval) + "%";
                        }
                        InspectCheckResult usage = new InspectCheckResult();
                        usage.setReportId(reportId);
                        usage.setSection("DB_USAGE");
                        usage.setItemName(cat);
                        usage.setResultText(usageText);
                        usage.setSortOrder(usageSort++);
                        checkResultRepository.save(usage);
                    }
                }
                // ap.os.disk_summary: drives 맵 → AP_USAGE 행 생성
                if ("ap.os.disk_summary".equals(key) && value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> drives = (Map<String, Object>) value;
                    Map<String, String> driveLabels = Map.of("c", "Disk C:", "d", "Disk D:");
                    int diskSort = 2;
                    for (var dEntry : drives.entrySet()) {
                        String label = driveLabels.getOrDefault(dEntry.getKey(), "Disk " + dEntry.getKey().toUpperCase() + ":");
                        String diskText = "";
                        if (dEntry.getValue() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> dd = (Map<String, Object>) dEntry.getValue();
                            Object t = dd.get("t"); Object f = dd.get("f");
                            diskText = (t != null && f != null) ? t + "GB / " + f + "GB 여유" : dd.get("p") + "%";
                        }
                        InspectCheckResult usage = new InspectCheckResult();
                        usage.setReportId(reportId);
                        usage.setSection("AP_USAGE");
                        usage.setItemName(label);
                        usage.setResultText(diskText);
                        usage.setSortOrder(diskSort++);
                        checkResultRepository.save(usage);
                    }
                }
            }
        }
        return stats;
    }

    /**
     * v6 — agent snapshot 의 AP/DB tier 에서 cpu/mem/disk_pct 추출 → inspect_metric_snapshot UPSERT.
     * GIS 는 시계열 누적 대상 아님 (사용자 결정 D-1). 멱등 적재 (ON CONFLICT DO NOTHING).
     *
     * <p>collected_at 은 payload.ts (unix seconds) 우선, 없으면 now(). DB팀 D-4 권고.
     * <p>실패 정책: warn-only — 호출자가 try/catch 로 감싸 batch upload 자체는 성공 (DB팀 D-5).
     */
    private void saveMetricSnapshot(InspectionQrBatchRequest.Payload payload, Long pjtId) {
        if (payload.getTiers() == null) return;

        OffsetDateTime collectedAt = (payload.getTs() != null)
                ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(payload.getTs()), ZoneOffset.UTC)
                : OffsetDateTime.now();

        for (String roleKey : new String[]{"ap", "db"}) {
            InspectionQrBatchRequest.Tier tier = payload.getTiers().get(roleKey);
            if (tier == null || tier.getItems() == null) continue;

            String hostName = tier.getH();   // QrBatchPayloadAdapter 가 per-tier host 를 tier.h 로 정규화
            String hostIp   = null;          // payload schema 에 ip 없음 — 향후 host detail 확장 시 추가

            Double cpu = null, mem = null, disk = null;
            for (List<Object> metric : tier.getItems()) {
                if (metric == null || metric.size() < 3) continue;
                String key   = Objects.toString(metric.get(0), null);
                Object value = metric.get(2);
                if (key == null) continue;

                Double num = InspectQrMetricSupport.extractPercent(value);
                if (num == null) continue;

                // 매핑 — agent ps1 의 실제 ID (manifest.json 참조)
                //   AP: ap.perf.cpu_pct / ap.perf.mem_pct / ap.os.disk_summary
                //   DB: db.os.perf_cpu / db.os.perf_mem / db.os.disk
                if (key.endsWith(".perf.cpu_pct") || key.endsWith(".os.perf_cpu") || key.endsWith(".perf_cpu")) {
                    cpu = num;
                } else if (key.endsWith(".perf.mem_pct") || key.endsWith(".os.perf_mem") || key.endsWith(".perf_mem")) {
                    mem = num;
                } else if (key.endsWith(".disk_summary") || key.endsWith(".os.disk")) {
                    disk = num;
                }
            }

            if (cpu == null && mem == null && disk == null) {
                log.debug("metric snapshot skip — no perf data: pjt={} role={}", pjtId, roleKey);
                continue;
            }

            String rawJson;
            try {
                rawJson = HASH_MAPPER.writeValueAsString(tier);
            } catch (JsonProcessingException e) {
                rawJson = "{\"_serialize_error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
            }

            int affected = metricSnapshotRepository.upsertIgnore(
                    pjtId,
                    roleKey.toUpperCase(Locale.ROOT),
                    hostName,
                    hostIp,
                    collectedAt,
                    cpu  != null ? BigDecimal.valueOf(cpu)  : null,
                    mem  != null ? BigDecimal.valueOf(mem)  : null,
                    disk != null ? BigDecimal.valueOf(disk) : null,
                    rawJson);
            log.info("metric snapshot pjt={} role={} host={} at={} cpu={} mem={} disk={} affected={}",
                    pjtId, roleKey, hostName, collectedAt, cpu, mem, disk, affected);
        }
    }

    private InspectQrBatch buildBatch(InspectionQrBatchRequest.Payload payload,
                                      InspectionQrBatchRequest.Header header,
                                      Long reportId,
                                      Long userId,
                                      String hashCheck) {
        InspectQrBatch batch = new InspectQrBatch();
        batch.setPayloadId(payload.getId());
        batch.setReportId(reportId);
        batch.setSiteCode(payload.getSite());
        batch.setInspectRound(payload.getRound());
        batch.setPayloadTs(payload.getTs());
        batch.setSourceInspector(payload.getInspector());
        batch.setHeaderHash(header.getHash());
        batch.setRawBytes(header.getRawBytes());
        batch.setGzBytes(header.getGzBytes());
        batch.setPayloadJson(HASH_MAPPER.convertValue(payload, Map.class));
        batch.setHashCheck(hashCheck);
        batch.setUploadedBy(userId);
        return batch;
    }

    /**
     * payload 를 Python encode.py 와 가능한 한 동일하게 직렬화 → gzip → SHA-1 hex[:6] 계산 →
     * header.hash 와 비교. 불일치는 warn-only (NFR-3) — 응답에 표지, 예외는 발생시키지 않음.
     */
    String verifyHash(InspectionQrBatchRequest.Payload payload, String headerHash) {
        if (headerHash == null || headerHash.isBlank()) return "skip";
        try {
            String json = HASH_MAPPER.writeValueAsString(payload);
            byte[] gz = gzip(json.getBytes(StandardCharsets.UTF_8));
            String sha = sha1Hex(gz);
            String shaPrefix = sha.substring(0, 6);
            if (shaPrefix.equalsIgnoreCase(headerHash)) {
                return "ok";
            }
            log.warn("inspection-qr-batch hash mismatch header={} computed={} payload_id={}",
                    headerHash, shaPrefix, payload.getId());
            return "warn";
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            log.warn("inspection-qr-batch hash verify failed payload_id={} reason={}", payload.getId(), e.toString());
            return "skip";
        } catch (Exception e) {
            log.warn("inspection-qr-batch hash verify unexpected payload_id={} reason={}", payload.getId(), e.toString());
            return "skip";
        }
    }

    private static byte[] gzip(byte[] data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            gz.write(data);
            gz.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String sha1Hex(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(data);
        return HexFormat.of().formatHex(digest);
    }

    private InspectionQrBatchResponse toIdempotentResponse(InspectQrBatch batch) {
        Long pjtId = null;
        Optional<SwProject> pjt = swProjectRepository.findBySiteCode(batch.getSiteCode())
                .or(() -> swProjectRepository.findFirstBySiteCodeAlias(batch.getSiteCode()));
        if (pjt.isPresent()) pjtId = pjt.get().getProjId();

        int tierCount = 0, itemCount = 0, manual = 0, warn = 0;
        Map<String, Object> json = batch.getPayloadJson();
        if (json != null) {
            Object tiersObj = json.get("tiers");
            if (tiersObj instanceof Map<?, ?> tiersMap) {
                tierCount = tiersMap.size();
                for (Object tier : tiersMap.values()) {
                    if (tier instanceof Map<?, ?> t) {
                        Object items = t.get("i");
                        if (items instanceof List<?> list) {
                            for (Object metric : list) {
                                if (metric instanceof List<?> tuple && tuple.size() >= 2) {
                                    itemCount++;
                                    Object status = tuple.get(1);
                                    if (status != null) {
                                        String s = status.toString();
                                        if ("M".equalsIgnoreCase(s)) manual++;
                                        else if ("warn".equalsIgnoreCase(s)) warn++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return InspectionQrBatchResponse.builder()
                .reportId(batch.getReportId())
                .batchId(batch.getPayloadId())
                .idempotent(true)
                .pjtId(pjtId)
                .tierCount(tierCount)
                .itemCount(itemCount)
                .manualItems(manual)
                .warnItems(warn)
                .hashCheck(batch.getHashCheck())
                .reportUrl(batch.getReportId() == null ? null : "/document/inspect/" + batch.getReportId())
                .build();
    }

    // ── value types ─────────────────────────────────────────────────────────

    private static final class ItemStats {
        int total;
        int manual;
        int warn;
    }
}
