package com.swmanager.system.service.inspection;

import com.swmanager.system.constant.enums.InspectResultCode;
import com.swmanager.system.service.inspection.InspectionQrBatchService.ResultText;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.swmanager.system.service.inspection.InspectionQrBatchService.buildRemarks;
import static com.swmanager.system.service.inspection.InspectionQrBatchService.extractPercent;
import static com.swmanager.system.service.inspection.InspectionQrBatchService.formatValue;
import static com.swmanager.system.service.inspection.InspectionQrBatchService.formatValueWithContext;
import static com.swmanager.system.service.inspection.InspectionQrBatchService.parseNumeric;
import static com.swmanager.system.service.inspection.InspectionQrBatchService.resolveSection;
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

    // ── parseNumeric: 비숫자 제거 후 파싱 ──
    @Test void parseNumeric_cleansAndParses() {
        assertThat(parseNumeric(null)).isNull();
        assertThat(parseNumeric("52%")).isEqualTo(52.0);
        assertThat(parseNumeric("1,234")).isEqualTo(1234.0);       // 콤마 제거
        assertThat(parseNumeric("-3.5C")).isEqualTo(-3.5);
        assertThat(parseNumeric("abc")).isNull();                   // 숫자 없음 → null
        assertThat(parseNumeric("")).isNull();
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
