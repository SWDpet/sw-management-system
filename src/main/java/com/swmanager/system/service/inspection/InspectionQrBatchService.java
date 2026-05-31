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

    private static final int RESULT_TEXT_MAX = 500;

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

    /**
     * manifest.json ID → (section, sort) 매핑.
     * inspect_template seed 의 sort_order 와 정합 — UI 행 위치 결정.
     */
    private static final Map<String, int[]> MANIFEST_SORT = new java.util.LinkedHashMap<>();
    static {
        // AP 14 항목 (sort 0-based for UI row index)
        MANIFEST_SORT.put("ap.led.system",       new int[]{0});
        MANIFEST_SORT.put("ap.led.psu",           new int[]{1});
        MANIFEST_SORT.put("ap.led.disk",          new int[]{2});
        MANIFEST_SORT.put("ap.hw.cpu",            new int[]{3});
        MANIFEST_SORT.put("ap.hw.memory",         new int[]{4});
        MANIFEST_SORT.put("ap.hw.adapter",        new int[]{5});
        MANIFEST_SORT.put("ap.log.system_err",    new int[]{6});
        MANIFEST_SORT.put("ap.log.security_err",  new int[]{7});
        MANIFEST_SORT.put("ap.os.disk_summary",   new int[]{8});
        MANIFEST_SORT.put("ap.disk.c",            new int[]{8});  // alias
        MANIFEST_SORT.put("ap.disk.d",            new int[]{8});  // alias
        MANIFEST_SORT.put("ap.net.routes",        new int[]{9});
        MANIFEST_SORT.put("ap.net.ip",            new int[]{10});
        MANIFEST_SORT.put("ap.security.users",    new int[]{11});
        MANIFEST_SORT.put("ap.perf.cpu_pct",      new int[]{12});
        MANIFEST_SORT.put("ap.perf.mem_pct",      new int[]{13});
        MANIFEST_SORT.put("ap.cable",             new int[]{5});  // adapter alias
        // DB 24 항목
        MANIFEST_SORT.put("db.led.hw",            new int[]{0});
        MANIFEST_SORT.put("db.led.disk",          new int[]{1});
        MANIFEST_SORT.put("db.led.fan",           new int[]{2});
        MANIFEST_SORT.put("db.led.power",         new int[]{3});
        MANIFEST_SORT.put("db.led.cable",         new int[]{4});
        MANIFEST_SORT.put("db.os.cpu_info",       new int[]{5});
        MANIFEST_SORT.put("db.os.mem_info",       new int[]{6});
        MANIFEST_SORT.put("db.os.adapter",        new int[]{7});
        MANIFEST_SORT.put("db.os.disk",           new int[]{8});
        MANIFEST_SORT.put("db.os.network_ip",     new int[]{9});
        MANIFEST_SORT.put("db.os.perf_cpu",       new int[]{10});
        MANIFEST_SORT.put("db.os.perf_mem",       new int[]{11});
        MANIFEST_SORT.put("db.os.swap_pct",       new int[]{12});
        MANIFEST_SORT.put("db.os.iostat",         new int[]{13});
        MANIFEST_SORT.put("db.os.netstat_perf",   new int[]{14});
        MANIFEST_SORT.put("db.os.disk_data",      new int[]{15});
        MANIFEST_SORT.put("db.os.inode",          new int[]{16});
        MANIFEST_SORT.put("db.os.lsvg_rootvg",    new int[]{17});
        MANIFEST_SORT.put("db.os.net_link",       new int[]{18});
        MANIFEST_SORT.put("db.os.net_ping",       new int[]{19});
        MANIFEST_SORT.put("db.os.net_collisions", new int[]{20});
        MANIFEST_SORT.put("db.os.processor",      new int[]{21});
        MANIFEST_SORT.put("db.os.log_system",     new int[]{22});
        MANIFEST_SORT.put("db.os.uptime",         new int[]{22}); // 프로세서 상태 대체
        MANIFEST_SORT.put("db.os.users",          new int[]{23});
        // DBMS 17 항목
        MANIFEST_SORT.put("db.oracle.archive_mode",     new int[]{0});
        MANIFEST_SORT.put("db.oracle.alert_errors_24h", new int[]{1});
        MANIFEST_SORT.put("db.oracle.redo_logs",        new int[]{2});
        MANIFEST_SORT.put("db.oracle.sga",              new int[]{3});
        MANIFEST_SORT.put("db.oracle.pga",              new int[]{4});
        MANIFEST_SORT.put("db.oracle.tablespace",       new int[]{5});
        MANIFEST_SORT.put("db.oracle.datafile_status",   new int[]{6});
        MANIFEST_SORT.put("db.oracle.invalid_objects",   new int[]{7});
        MANIFEST_SORT.put("db.oracle.sessions",          new int[]{8});
        MANIFEST_SORT.put("db.oracle.wait_events",       new int[]{9});
        MANIFEST_SORT.put("db.oracle.rman_backup",       new int[]{10});
        MANIFEST_SORT.put("db.oracle.export_last",       new int[]{11});
        MANIFEST_SORT.put("db.oracle.standby_lag",       new int[]{12});
        MANIFEST_SORT.put("db.oracle.param_modified",    new int[]{13});
        MANIFEST_SORT.put("db.oracle.undo",              new int[]{14});
        MANIFEST_SORT.put("db.oracle.temp",              new int[]{15});
        MANIFEST_SORT.put("db.oracle.controlfile",       new int[]{16});
        // GIS 12 항목
        MANIFEST_SORT.put("gis.gss.running",             new int[]{0});
        MANIFEST_SORT.put("gis.gss.log_purge",           new int[]{1});
        MANIFEST_SORT.put("gis.desktop.gss_store",       new int[]{2});
        MANIFEST_SORT.put("gis.gws.running",             new int[]{3});
        MANIFEST_SORT.put("gis.gws.log_purge",           new int[]{4});
        MANIFEST_SORT.put("gis.gws.http",                new int[]{5});
        MANIFEST_SORT.put("gis.gws.store_size",          new int[]{6});
        MANIFEST_SORT.put("gis.gss.err_30days",          new int[]{7});
        MANIFEST_SORT.put("gis.gss.warn_30days",         new int[]{8});
        MANIFEST_SORT.put("gis.gws.catalina_err",        new int[]{9});
        MANIFEST_SORT.put("gis.gws.stdout_log_size",     new int[]{10});
        MANIFEST_SORT.put("gis.uwes.dem_slop_preserved", new int[]{11});
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

                String section = resolveSection(key);
                int[] mapped = key != null ? MANIFEST_SORT.get(key) : null;
                int sortOrder;
                if (mapped != null) {
                    sortOrder = mapped[0];
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

                ResultText resultText = formatValueWithContext(key, value);
                String rtText = resultText.text;
                if (rtText != null && rtText.length() > RESULT_TEXT_MAX) {
                    rtText = rtText.substring(0, RESULT_TEXT_MAX);
                }
                row.setResultText(rtText);
                row.setRemarks(buildRemarks(status, code, resultText.truncated));
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

    private static String resolveSection(String itemId) {
        if (itemId == null) return "AP";
        if (itemId.startsWith("db.oracle.")) return "DBMS";
        if (itemId.startsWith("db."))        return "DB";
        if (itemId.startsWith("gis."))       return "GIS";
        return "AP";
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

                Double num = extractPercent(value);
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

    /** value (Number | Map | String) 에서 % 숫자 추출. 못 찾으면 null. */
    private static Double extractPercent(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof Map<?, ?> map) {
            for (String k : new String[]{"used_pct", "pct", "worst_pct"}) {
                Object v = map.get(k);
                if (v instanceof Number) return ((Number) v).doubleValue();
                if (v != null) {
                    Double d = parseNumeric(v.toString());
                    if (d != null) return d;
                }
            }
            return null;
        }
        return parseNumeric(value.toString());
    }

    private static Double parseNumeric(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("[^\\d.\\-]", "");
        if (cleaned.isEmpty()) return null;
        try { return Double.parseDouble(cleaned); }
        catch (Exception e) { return null; }
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

    private static ResultText formatValue(Object value) {
        if (value == null) return new ResultText("", false);
        String raw = String.valueOf(value);
        if (raw.length() <= RESULT_TEXT_MAX) return new ResultText(raw, false);
        return new ResultText(raw.substring(0, RESULT_TEXT_MAX), true);
    }

    private static final Map<String, String> UNIT_LABELS = Map.ofEntries(
        Map.entry("ap.hw.cpu", "코어"), Map.entry("ap.hw.memory", "GB"),
        Map.entry("ap.hw.adapter", "개"), Map.entry("ap.cable", "개"),
        Map.entry("ap.log.system_err", "건"), Map.entry("ap.log.security_err", "건"),
        Map.entry("ap.security.users", "계정"),
        Map.entry("ap.net.routes", "개"), Map.entry("ap.net.ip", "개"),
        Map.entry("db.os.cpu_info", "코어"), Map.entry("db.os.mem_info", "GB"),
        Map.entry("db.os.adapter", "개"), Map.entry("db.os.disk", "개"),
        Map.entry("db.os.network_ip", "개"), Map.entry("db.os.users", "계정"),
        Map.entry("db.os.uptime", "일"),
        Map.entry("db.oracle.alert_errors_24h", "건"), Map.entry("db.oracle.invalid_objects", "개"),
        Map.entry("db.oracle.sessions", "세션"), Map.entry("db.oracle.wait_events", "건"),
        Map.entry("db.oracle.rman_backup", "건"), Map.entry("db.oracle.export_last", "건"),
        Map.entry("db.oracle.redo_logs", "그룹"), Map.entry("db.oracle.controlfile", "개"),
        Map.entry("db.oracle.param_modified", "개"),
        Map.entry("gis.gss.running", "프로세스"), Map.entry("gis.gws.running", "서비스"),
        Map.entry("gis.gss.log_purge", "파일 삭제"), Map.entry("gis.gws.log_purge", "파일 삭제"),
        Map.entry("gis.gss.err_30days", "건"), Map.entry("gis.gss.warn_30days", "건"),
        Map.entry("gis.gws.catalina_err", "건"), Map.entry("gis.gws.stdout_log_size", "MB")
    );

    @SuppressWarnings("unchecked")
    private static ResultText formatValueWithContext(String key, Object value) {
        if (value == null) return new ResultText("", false);

        // 객체(Map) 형태의 value 처리
        if (key != null && value instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) value;
            // QR 축약 disk
            if (key.equals("ap.os.disk_summary")) {
                // drives 맵: {c:{t,f,p}, d:{t,f,p}}
                StringBuilder sb = new StringBuilder();
                for (var de : m.entrySet()) {
                    if (de.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dd = (Map<String, Object>) de.getValue();
                        sb.append(de.getKey().toUpperCase()).append(": ").append(dd.get("p")).append("% ");
                    }
                }
                return new ResultText(sb.toString().trim(), false);
            }
            if (key.startsWith("ap.disk.")) {
                Object t = m.get("t"); Object f = m.get("f");
                if (t == null) t = m.get("total_gb");
                if (f == null) f = m.get("free_gb");
                if (t != null && f != null) return new ResultText(t + "GB / " + f + "GB 여유", false);
                Object p = m.get("p");
                if (p == null) p = m.get("pct");
                if (p != null) return new ResultText(p + "%", false);
            }
            if (key.equals("db.os.disk")) {
                // mounts 맵: {"/":{p:52,t:5,f:2.4}, "/oracle":{...}, ...}
                Object rootMount = m.get("/");
                if (rootMount instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rm = (Map<String, Object>) rootMount;
                    return new ResultText("worst " + m.size() + "개 마운트", false);
                }
                Object fsList = m.get("filesystems");
                if (fsList instanceof List && !((List<?>) fsList).isEmpty()) {
                    return new ResultText(((List<?>) fsList).size() + "개 파일시스템", false);
                }
            }
            Object pct = m.get("pct");
            if (pct == null) pct = m.get("used_pct");
            if (pct != null) return new ResultText(pct + "%", false);
            Object count = m.get("count");
            if (count != null) {
                String unit = key != null ? UNIT_LABELS.getOrDefault(key, "개") : "개";
                return new ResultText(count + unit, false);
            }
            // 항목별 핵심값 + 단위 추출
            String memo = null;
            if (key != null) {
                memo = switch (key) {
                    case "ap.hw.cpu" -> { Object c=m.get("cores"); Object n=m.get("name"); yield (n!=null?n+" ":"")+(c!=null?c+"코어":""); }
                    case "ap.hw.memory" -> { Object g=m.get("installed_gb"); yield g!=null?g+"GB":"-"; }
                    case "ap.hw.adapter" -> { Object u=m.get("up"); Object t=m.get("total"); yield u+"개 UP / "+t+"개"; }
                    case "ap.os.disk_summary" -> { Object s=m.get("summary"); yield s!=null?String.valueOf(s):"-"; }
                    case "db.os.cpu_info" -> { Object c=m.get("cores"); Object g=m.get("clock_ghz"); yield (c!=null?c+"코어":"")+(g!=null?" "+g+"GHz":""); }
                    case "db.os.mem_info" -> { Object g=m.get("total_gb"); yield g!=null?g+"GB":"-"; }
                    case "db.os.adapter" -> { Object a=m.get("adapter_count"); yield a!=null?a+"개":"-"; }
                    case "db.os.network_ip" -> { Object ip = m.get("ipv4"); yield ip instanceof List && !((List<?>)ip).isEmpty() ? String.valueOf(((List<?>)ip).get(0)) : "-"; }
                    case "ap.net.ip" -> { Object ip = m.get("ips"); yield ip instanceof List && !((List<?>)ip).isEmpty() ? String.valueOf(((Map<?,?>)((List<?>)ip).get(0)).get("IPAddress")) : "-"; }
                    case "db.os.netstat_perf" -> m.get("total_errs") + "건";
                    case "db.os.iostat" -> m.get("stderr") != null ? "error" : "정상";
                    case "db.os.net_ping" -> { Object o=m.get("note"); yield o != null && String.valueOf(o).contains("not found") ? "정상 (ping OK)" : "-"; }
                    case "db.os.net_link" -> m.get("established") + "건";
                    case "db.os.net_collisions" -> m.get("total_coll") + "건";
                    case "db.os.lsvg_rootvg" -> m.get("stale_count") + "건";
                    case "db.oracle.wait_events" -> { Object te=m.get("top_events"); yield te instanceof List ? ((List<?>)te).size()+"건" : "-"; }
                    case "db.oracle.export_last" -> m.get("last_export") != null ? String.valueOf(m.get("last_export")) : "없음";
                    case "db.oracle.standby_lag" -> m.get("apply_lag") != null ? String.valueOf(m.get("apply_lag")) : "N/A";
                    case "db.oracle.datafile_status" -> m.get("total") + "개";
                    case "gis.gws.running" -> String.valueOf(m.getOrDefault("status", "-"));
                    case "gis.gws.http" -> m.get("http_status") + "";
                    case "gis.gws.store_size" -> m.get("total_gb") + "GB";
                    case "gis.gws.stdout_log_size" -> m.get("total_mb") + "MB";
                    case "gis.uwes.dem_slop_preserved" -> Boolean.TRUE.equals(m.get("both_exist")) ? "보존" : "누락";
                    case "ap.log.system_err", "ap.log.security_err" -> m.get("error_count") + "건";
                    case "ap.net.routes" -> m.get("routes") + "개";
                    case "ap.security.users" -> m.get("enabled") + "계정";
                    case "gis.gss.log_purge", "gis.gws.log_purge" -> m.get("purged_count") + "파일";
                    default -> null;
                };
            }
            if (memo != null) return new ResultText(memo, false);
            // 범용 fallback: 첫 번째 짧은 스칼라
            var skip = java.util.Set.of("os","output","raw","sid","rows","stderr","sample",
                    "cmd","method","note","excluded_rules","excluded_count","recent","since_days",
                    "threshold","remote_host","remote_user","source","path","config_name",
                    "dry_run","retain_days","exclude_patterns","window_days","rotation_hint",
                    "entries","interfaces","lvs","tablespaces","top_events","users","ips",
                    "adapters","discovered_paths","pids","active_users");
            for (var e : m.entrySet()) {
                if (skip.contains(e.getKey())) continue;
                Object v = e.getValue();
                if (v == null || v instanceof List || v instanceof Map) continue;
                String vs = String.valueOf(v);
                if (vs.length() > 30) continue;
                String unit = UNIT_LABELS.getOrDefault(key, "");
                return new ResultText(vs + unit, false);
            }
            // 아무것도 못 찾으면 빈 메모
            return new ResultText("", false);
        }

        // 퍼센트 항목 (QR 축약 시 pct 값만 전달되는 항목들)
        if (key != null && (key.contains(".perf.cpu") || key.contains(".perf.mem")
                || key.contains(".perf_cpu") || key.contains(".perf_mem")
                || key.contains(".swap_pct") || key.startsWith("ap.disk.")
                || key.endsWith(".sga") || key.endsWith(".pga")
                || key.endsWith(".tablespace") || key.endsWith(".undo") || key.endsWith(".temp"))) {
            return new ResultText(value + "%", false);
        }

        // 단위 레이블이 있는 항목
        if (key != null && UNIT_LABELS.containsKey(key)) {
            return new ResultText(value + UNIT_LABELS.get(key), false);
        }

        return formatValue(value);
    }

    private static String buildRemarks(String status, InspectResultCode code, boolean truncated) {
        StringBuilder sb = new StringBuilder();
        if ("M".equalsIgnoreCase(status)) {
            sb.append("육안 점검 필요 (자동수집 불가)");
        } else if (code != null && code == InspectResultCode.NORMAL
                && status != null
                && !"ok".equalsIgnoreCase(status)
                && !"normal".equalsIgnoreCase(status)) {
            sb.append("알 수 없는 상태 fallback: ").append(status);
        }
        if (truncated) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append("(truncated)");
        }
        return sb.length() == 0 ? null : sb.toString();
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

    private record ResultText(String text, boolean truncated) {}
}
