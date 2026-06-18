package com.swmanager.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExcelExportService 절사 단위(rounddownUnit) 순수 로직 골든 테스트 (S3).
 *
 * 대상: normalizeRounddownUnit / toRoundDigits / roundLabel (package-private).
 * 배경: 절사 로직은 과거 결함 이력(V015_fix_rounddown_units.sql)이 있는데도 단위 테스트 0건이었음.
 * 견적서와 동일 매핑(백/천/만/십만)을 회귀 고정한다.
 */
class ExcelExportServiceRounddownTest {

    // ── normalizeRounddownUnit: 허용값은 그대로 ──
    @ParameterizedTest
    @ValueSource(ints = {1000, 10000, 100000, 1000000})
    void normalize_allowedUnits_passThrough(int unit) {
        assertThat(ExcelExportService.normalizeRounddownUnit(unit)).isEqualTo(unit);
        assertThat(ExcelExportService.normalizeRounddownUnit(String.valueOf(unit))).isEqualTo(unit);
    }

    // ── normalizeRounddownUnit: null/빈값/0 → 0 (절사 안 함) ──
    @Test
    void normalize_nullOrBlankOrZero_returnsZero() {
        assertThat(ExcelExportService.normalizeRounddownUnit(null)).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit("")).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit("   ")).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit(0)).isZero();
        assertThat(ExcelExportService.normalizeRounddownUnit("0")).isZero();
    }

    // ── normalizeRounddownUnit: 허용외 숫자/파싱불가 → 1000 fallback ──
    @ParameterizedTest
    @CsvSource({"500", "9999", "12345", "999999999"})
    void normalize_disallowedNumber_fallbacksTo1000(String raw) {
        assertThat(ExcelExportService.normalizeRounddownUnit(raw)).isEqualTo(1000);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1,000", "10000원", "１０００"})
    void normalize_unparseable_fallbacksTo1000(String raw) {
        assertThat(ExcelExportService.normalizeRounddownUnit(raw)).isEqualTo(1000);
    }

    // ── toRoundDigits: 단위 → ROUNDDOWN digits ──
    @Test
    void toRoundDigits_mapsEachUnit() {
        assertThat(ExcelExportService.toRoundDigits(1000)).isEqualTo(-3);
        assertThat(ExcelExportService.toRoundDigits(10000)).isEqualTo(-4);
        assertThat(ExcelExportService.toRoundDigits(100000)).isEqualTo(-5);
        assertThat(ExcelExportService.toRoundDigits(1000000)).isEqualTo(-6);
        // 정의 외 입력은 -3(백단위) fallback
        assertThat(ExcelExportService.toRoundDigits(7)).isEqualTo(-3);
    }

    // ── roundLabel: 단위 → 비고 라벨 ──
    @Test
    void roundLabel_mapsEachUnit() {
        assertThat(ExcelExportService.roundLabel(1000)).isEqualTo("백단위 절사");
        assertThat(ExcelExportService.roundLabel(10000)).isEqualTo("천단위 절사");
        assertThat(ExcelExportService.roundLabel(100000)).isEqualTo("만단위 절사");
        assertThat(ExcelExportService.roundLabel(1000000)).isEqualTo("십만단위 절사");
        assertThat(ExcelExportService.roundLabel(42)).isEqualTo("백단위 절사");
    }

    // ── digits ↔ label 정합성: 같은 단위면 둘 다 같은 자릿수 의미 ──
    @Test
    void digitsAndLabel_consistentForAllUnits() {
        int[] units = {1000, 10000, 100000, 1000000};
        String[] labels = {"백단위 절사", "천단위 절사", "만단위 절사", "십만단위 절사"};
        int[] digits = {-3, -4, -5, -6};
        for (int i = 0; i < units.length; i++) {
            assertThat(ExcelExportService.toRoundDigits(units[i])).isEqualTo(digits[i]);
            assertThat(ExcelExportService.roundLabel(units[i])).isEqualTo(labels[i]);
        }
    }
}
