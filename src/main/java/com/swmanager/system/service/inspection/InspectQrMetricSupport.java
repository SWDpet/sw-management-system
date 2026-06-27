package com.swmanager.system.service.inspection;

import com.swmanager.system.constant.enums.InspectResultCode;

import java.util.List;
import java.util.Map;

/**
 * 점검 QR batch payload 의 metric value 변환 support (순수 함수).
 *
 * InspectionQrBatchService(S4 거대클래스 #3) 에서 분리된 무상태 변환 로직:
 * <ul>
 *   <li><b>A. 값 포맷팅</b>: payload metric value → 표시 텍스트 ({@link #formatValue}, {@link #formatValueWithContext}, {@link #buildRemarks})</li>
 *   <li><b>B. 섹션·정렬 매핑</b>: 항목 ID → section / sort_order ({@link #resolveSection}, {@link #manifestSort})</li>
 *   <li><b>C. 숫자 추출</b>: Number/Map/String → Double ({@link #extractPercent}, {@link #parseNumeric})</li>
 * </ul>
 *
 * 기획서: docs/product-specs/refactor-inspection-qr-batch-split.md
 * 개발계획서: docs/exec-plans/refactor-inspection-qr-batch-split.md
 *
 * <p>모두 필드/repo 의존 0 의 순수 변환 — package-private static. hash 검증(HASH_MAPPER 공유)과
 * DB orchestration 은 InspectionQrBatchService 에 잔류.
 */
final class InspectQrMetricSupport {

    private InspectQrMetricSupport() {}

    static final int RESULT_TEXT_MAX = 500;

    // ── B. 섹션·정렬 매핑 ─────────────────────────────────────────────────────

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

    /**
     * 항목 ID 의 manifest sort_order(0-based) 반환. 매핑 없으면 null.
     * {@code int[]} 원시구조를 노출하지 않아 외부 변조를 차단(접근 메서드).
     */
    static Integer manifestSort(String key) {  // package-private: 단위 테스트 + 서비스 호출
        if (key == null) return null;
        int[] mapped = MANIFEST_SORT.get(key);
        return mapped == null ? null : mapped[0];
    }

    static String resolveSection(String itemId) {  // package-private: 단위 테스트(순수 함수)
        if (itemId == null) return "AP";
        if (itemId.startsWith("db.oracle.")) return "DBMS";
        if (itemId.startsWith("db."))        return "DB";
        if (itemId.startsWith("gis."))       return "GIS";
        return "AP";
    }

    // ── C. 숫자 추출 ──────────────────────────────────────────────────────────

    /** value (Number | Map | String) 에서 % 숫자 추출. 못 찾으면 null. */
    static Double extractPercent(Object value) {  // package-private: 단위 테스트
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

    static Double parseNumeric(String s) {  // package-private: 단위 테스트
        if (s == null) return null;
        String cleaned = s.replaceAll("[^\\d.\\-]", "");
        if (cleaned.isEmpty()) return null;
        try { return Double.parseDouble(cleaned); }
        catch (Exception e) { return null; }
    }

    // ── A. 값 포맷팅 ──────────────────────────────────────────────────────────

    static ResultText formatValue(Object value) {  // package-private: 단위 테스트
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
    static ResultText formatValueWithContext(String key, Object value) {  // package-private: 단위 테스트
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

    static String buildRemarks(String status, InspectResultCode code, boolean truncated) {  // package-private: 단위 테스트
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

    // ── value type ───────────────────────────────────────────────────────────

    record ResultText(String text, boolean truncated) {}  // package-private: 단위 테스트 검증
}
