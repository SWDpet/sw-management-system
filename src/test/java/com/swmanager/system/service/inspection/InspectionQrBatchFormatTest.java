package com.swmanager.system.service.inspection;

import com.swmanager.system.constant.enums.InspectResultCode;
import com.swmanager.system.service.inspection.InspectQrMetricSupport.ResultText;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.swmanager.system.service.inspection.InspectQrMetricSupport.buildRemarks;
import static com.swmanager.system.service.inspection.InspectQrMetricSupport.extractPercent;
import static com.swmanager.system.service.inspection.InspectQrMetricSupport.formatValue;
import static com.swmanager.system.service.inspection.InspectQrMetricSupport.formatValueWithContext;
import static com.swmanager.system.service.inspection.InspectQrMetricSupport.manifestSort;
import static com.swmanager.system.service.inspection.InspectQrMetricSupport.parseNumeric;
import static com.swmanager.system.service.inspection.InspectQrMetricSupport.resolveSection;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * InspectionQrBatchService 순수 포맷팅/파싱 헬퍼 단위 테스트 (PIT 보강).
 *
 * 점검 결과값을 사용자 화면 문자열로 변환하는 순수 static 헬퍼들을 직접 검증한다
 * (기존 통합 테스트는 흐름만 돌려 이 로직들이 대부분 NO_COVERAGE/SURVIVED 였음).
 */
class InspectionQrBatchFormatTest {

    // ── resolveSection: itemId prefix → 섹션 ──
    @Test void resolveSection_byPrefix() {
        assertThat(resolveSection(null)).isEqualTo("AP");
        assertThat(resolveSection("db.oracle.sessions")).isEqualTo("DBMS");
        assertThat(resolveSection("db.os.disk")).isEqualTo("DB");
        assertThat(resolveSection("gis.gws.running")).isEqualTo("GIS");
        assertThat(resolveSection("ap.hw.cpu")).isEqualTo("AP");
        assertThat(resolveSection("unknown.x")).isEqualTo("AP");
    }

    // ── manifestSort: itemId → sort_order(0-based) / 미매핑·null → null ──
    @Test void manifestSort_mappedAndNull() {
        assertThat(manifestSort("ap.led.system")).isEqualTo(0);
        assertThat(manifestSort("db.os.users")).isEqualTo(23);
        assertThat(manifestSort("ap.cable")).isEqualTo(5);    // adapter alias
        assertThat(manifestSort("no.such.key")).isNull();      // 미매핑 → fallback 분기
        assertThat(manifestSort(null)).isNull();
    }

    // ── parseNumeric: 비숫자 제거 후 파싱. 다중구분자는 null(§8-3 dual-review: malformed 비마스킹) ──
    @Test void parseNumeric_cleansAndParses() {
        assertThat(parseNumeric(null)).isNull();
        assertThat(parseNumeric("52%")).isEqualTo(52.0);
        assertThat(parseNumeric("1,234")).isEqualTo(1234.0);       // 콤마(천단위) 제거
        assertThat(parseNumeric("-3.5C")).isEqualTo(-3.5);
        assertThat(parseNumeric("abc")).isNull();                   // 숫자 없음 → null
        assertThat(parseNumeric("")).isNull();
        assertThat(parseNumeric("1.2.3")).isNull();                 // 다중 소수점 → null(malformed 노출)
        assertThat(parseNumeric("2026-05-01")).isNull();            // 날짜 구조 → null(숫자 오인 방지)
    }

    // ── extractPercent: Number/Map/String → Double ──
    @Test void extractPercent_fromNumber() {
        assertThat(extractPercent(42)).isEqualTo(42.0);
        assertThat(extractPercent(null)).isNull();
    }
    @Test void extractPercent_fromMap_keysInOrder() {
        assertThat(extractPercent(Map.of("used_pct", 80))).isEqualTo(80.0);
        assertThat(extractPercent(Map.of("pct", 70))).isEqualTo(70.0);
        assertThat(extractPercent(Map.of("worst_pct", "65%"))).isEqualTo(65.0);  // String → parseNumeric
        assertThat(extractPercent(Map.of("other", 1))).isNull();                 // 매칭 키 없음
    }
    @Test void extractPercent_fromString() {
        assertThat(extractPercent("90%")).isEqualTo(90.0);
        assertThat(extractPercent("n/a")).isNull();
    }

    // ── formatValue: 길이 절단 ──
    @Test void formatValue_truncatesOver500() {
        assertThat(formatValue(null)).isEqualTo(new ResultText("", false));
        assertThat(formatValue("short")).isEqualTo(new ResultText("short", false));

        String big = "x".repeat(600);
        ResultText r = formatValue(big);
        assertThat(r.truncated()).isTrue();
        assertThat(r.text()).hasSize(500);

        String exactly500 = "y".repeat(500);
        assertThat(formatValue(exactly500).truncated()).isFalse();  // 경계: 500 은 절단 안 함
    }

    // ── formatValueWithContext: 주요 분기 ──
    @Test void fvc_nullValue_empty() {
        assertThat(formatValueWithContext("ap.hw.cpu", null)).isEqualTo(new ResultText("", false));
    }
    @Test void fvc_diskSummary_drivesMap() {
        Map<String, Object> v = Map.of("c", Map.of("p", 52));
        assertThat(formatValueWithContext("ap.os.disk_summary", v))
                .isEqualTo(new ResultText("C: 52%", false));
    }
    @Test void fvc_apDisk_totalFree_andPct() {
        assertThat(formatValueWithContext("ap.disk.c", Map.of("t", 5, "f", 2)))
                .isEqualTo(new ResultText("5GB / 2GB 여유", false));
        assertThat(formatValueWithContext("ap.disk.c", Map.of("p", 52)))
                .isEqualTo(new ResultText("52%", false));
    }
    @Test void fvc_dbDisk_rootMount_andFilesystems() {
        assertThat(formatValueWithContext("db.os.disk", Map.of("/", Map.of("p", 52), "/oracle", Map.of("p", 30))))
                .isEqualTo(new ResultText("worst 2개 마운트", false));
        assertThat(formatValueWithContext("db.os.disk", Map.of("filesystems", List.of("a", "b"))))
                .isEqualTo(new ResultText("2개 파일시스템", false));
    }
    @Test void fvc_genericPct_andCount() {
        assertThat(formatValueWithContext("x.y", Map.of("pct", 77))).isEqualTo(new ResultText("77%", false));
        assertThat(formatValueWithContext("ap.cable", Map.of("count", 8))).isEqualTo(new ResultText("8개", false));
    }
    @Test void fvc_switchCases_sample() {
        assertThat(formatValueWithContext("ap.hw.cpu", Map.of("name", "Xeon", "cores", 8)))
                .isEqualTo(new ResultText("Xeon 8코어", false));
        assertThat(formatValueWithContext("ap.hw.memory", Map.of("installed_gb", 32)))
                .isEqualTo(new ResultText("32GB", false));
        assertThat(formatValueWithContext("db.os.cpu_info", Map.of("cores", 4, "clock_ghz", "2.4")))
                .isEqualTo(new ResultText("4코어 2.4GHz", false));
    }
    // ── switch arm null 가드: 키 부재 시 "null<단위>" 대신 "-" (§8-4) ──
    @Test void fvc_switchArm_nullGuard_returnsDash() {
        assertThat(formatValueWithContext("db.os.netstat_perf", Map.of())).isEqualTo(new ResultText("-", false));
        assertThat(formatValueWithContext("ap.hw.adapter", Map.of())).isEqualTo(new ResultText("-", false));
        assertThat(formatValueWithContext("gis.gws.store_size", Map.of())).isEqualTo(new ResultText("-", false));
        assertThat(formatValueWithContext("gis.gws.stdout_log_size", Map.of())).isEqualTo(new ResultText("-", false));
        // 값 존재 시 출력 보존 (회귀 0)
        assertThat(formatValueWithContext("db.os.netstat_perf", Map.of("total_errs", 5)))
                .isEqualTo(new ResultText("5건", false));
        assertThat(formatValueWithContext("ap.hw.adapter", Map.of("up", 2, "total", 3)))
                .isEqualTo(new ResultText("2개 UP / 3개", false));
    }
    // ── switch arm 표시값 전수 (미커버 분기 보강): Map 입력 → 단위 접미 ──
    // ⚠ pct/used_pct/count 키는 switch 진입 전 우선반환되므로 미포함
    @Test void fvc_switchArm_db_os() {
        assertThat(formatValueWithContext("db.os.mem_info", Map.of("total_gb", 64))).isEqualTo(new ResultText("64GB", false));
        assertThat(formatValueWithContext("db.os.adapter", Map.of("adapter_count", 4))).isEqualTo(new ResultText("4개", false));
        assertThat(formatValueWithContext("db.os.network_ip", Map.of("ipv4", List.of("10.0.0.1")))).isEqualTo(new ResultText("10.0.0.1", false));
        assertThat(formatValueWithContext("ap.net.ip", Map.of("ips", List.of(Map.of("IPAddress", "192.168.0.1"))))).isEqualTo(new ResultText("192.168.0.1", false));
        assertThat(formatValueWithContext("db.os.iostat", Map.of("stderr", "err"))).isEqualTo(new ResultText("error", false));
        assertThat(formatValueWithContext("db.os.iostat", Map.of("ok", 1))).isEqualTo(new ResultText("정상", false));
        assertThat(formatValueWithContext("db.os.net_ping", Map.of("note", "host not found"))).isEqualTo(new ResultText("정상 (ping OK)", false));
        assertThat(formatValueWithContext("db.os.net_ping", Map.of("note", "reachable"))).isEqualTo(new ResultText("-", false));
        assertThat(formatValueWithContext("db.os.net_link", Map.of("established", 3))).isEqualTo(new ResultText("3건", false));
        assertThat(formatValueWithContext("db.os.net_collisions", Map.of("total_coll", 2))).isEqualTo(new ResultText("2건", false));
        assertThat(formatValueWithContext("db.os.lsvg_rootvg", Map.of("stale_count", 1))).isEqualTo(new ResultText("1건", false));
    }
    @Test void fvc_switchArm_oracle() {
        assertThat(formatValueWithContext("db.oracle.wait_events", Map.of("top_events", List.of("a", "b")))).isEqualTo(new ResultText("2건", false));
        assertThat(formatValueWithContext("db.oracle.export_last", Map.of("last_export", "2026-05"))).isEqualTo(new ResultText("2026-05", false));
        assertThat(formatValueWithContext("db.oracle.export_last", Map.of("x", 1))).isEqualTo(new ResultText("없음", false));
        assertThat(formatValueWithContext("db.oracle.standby_lag", Map.of("apply_lag", "0s"))).isEqualTo(new ResultText("0s", false));
        assertThat(formatValueWithContext("db.oracle.standby_lag", Map.of("x", 1))).isEqualTo(new ResultText("N/A", false));
        assertThat(formatValueWithContext("db.oracle.datafile_status", Map.of("total", 5))).isEqualTo(new ResultText("5개", false));
    }
    @Test void fvc_switchArm_gis_ap() {
        assertThat(formatValueWithContext("gis.gws.running", Map.of("status", "UP"))).isEqualTo(new ResultText("UP", false));
        assertThat(formatValueWithContext("gis.gws.http", Map.of("http_status", 200))).isEqualTo(new ResultText("200", false));
        assertThat(formatValueWithContext("gis.uwes.dem_slop_preserved", Map.of("both_exist", true))).isEqualTo(new ResultText("보존", false));
        assertThat(formatValueWithContext("gis.uwes.dem_slop_preserved", Map.of("both_exist", false))).isEqualTo(new ResultText("누락", false));
        assertThat(formatValueWithContext("gis.gss.log_purge", Map.of("purged_count", 3))).isEqualTo(new ResultText("3파일", false));
        assertThat(formatValueWithContext("ap.log.system_err", Map.of("error_count", 7))).isEqualTo(new ResultText("7건", false));
        assertThat(formatValueWithContext("ap.net.routes", Map.of("routes", 10))).isEqualTo(new ResultText("10개", false));
        assertThat(formatValueWithContext("ap.security.users", Map.of("enabled", 2))).isEqualTo(new ResultText("2계정", false));
    }

    @Test void fvc_percentItems_byKeyPattern() {
        assertThat(formatValueWithContext("ap.perf.cpu", 55)).isEqualTo(new ResultText("55%", false));
        assertThat(formatValueWithContext("db.oracle.sga", 80)).isEqualTo(new ResultText("80%", false));
    }
    @Test void fvc_unitLabel_scalar() {
        assertThat(formatValueWithContext("ap.hw.cpu", 8)).isEqualTo(new ResultText("8코어", false));
    }
    @Test void fvc_fallback_plainScalar() {
        // key 매칭 없음, Map 아님 → formatValue 로 위임
        assertThat(formatValueWithContext("no.such.key", "정상")).isEqualTo(new ResultText("정상", false));
    }

    // ── buildRemarks ──
    @Test void buildRemarks_manualStatus() {
        assertThat(buildRemarks("M", null, false)).isEqualTo("육안 점검 필요 (자동수집 불가)");
    }
    @Test void buildRemarks_unknownStatusFallback() {
        assertThat(buildRemarks("weird", InspectResultCode.NORMAL, false))
                .isEqualTo("알 수 없는 상태 fallback: weird");
        // ok/normal 은 fallback 메시지 없음
        assertThat(buildRemarks("ok", InspectResultCode.NORMAL, false)).isNull();
        assertThat(buildRemarks("normal", InspectResultCode.NORMAL, false)).isNull();
    }
    @Test void buildRemarks_truncatedAppends() {
        assertThat(buildRemarks("ok", InspectResultCode.NORMAL, true)).isEqualTo("(truncated)");
        assertThat(buildRemarks("M", null, true)).isEqualTo("육안 점검 필요 (자동수집 불가) / (truncated)");
    }
    @Test void buildRemarks_emptyReturnsNull() {
        assertThat(buildRemarks("ok", InspectResultCode.NORMAL, false)).isNull();
        assertThat(buildRemarks(null, null, false)).isNull();
    }
}
