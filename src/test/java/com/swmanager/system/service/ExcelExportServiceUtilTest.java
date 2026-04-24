package com.swmanager.system.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 설계내역서 엑셀 — 유틸 함수 단위테스트.
 *
 * 기획서 v6 FR-11, FR-16, §7-8 (Acceptance) / 개발계획서 v6 T1~T4 대응.
 * 순수 JUnit (Spring context 로딩 없음).
 */
class ExcelExportServiceUtilTest {

    // ---------- T1: toExcelRoundDigitsArg 허용값 ----------
    @Test
    void toExcelRoundDigitsArg_returnsZeroOrNegativeForAllowedUnits() {
        assertThat(ExcelExportService.toExcelRoundDigitsArg(1)).isEqualTo(0);
        assertThat(ExcelExportService.toExcelRoundDigitsArg(10)).isEqualTo(-1);
        assertThat(ExcelExportService.toExcelRoundDigitsArg(100)).isEqualTo(-2);
        assertThat(ExcelExportService.toExcelRoundDigitsArg(1000)).isEqualTo(-3);
        assertThat(ExcelExportService.toExcelRoundDigitsArg(10000)).isEqualTo(-4);
    }

    // ---------- T2: toExcelRoundDigitsArg invalid 입력 ----------
    @Test
    void toExcelRoundDigitsArg_throwsForInvalidInput() {
        assertThatThrownBy(() -> ExcelExportService.toExcelRoundDigitsArg(500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rounddownUnit");
    }

    // ---------- T3: rounddownLabel 허용값 ----------
    @Test
    void rounddownLabel_returnsKoreanLabelsMatchingQuotationVatRules() {
        // quotation-vat-rules.md §43-51 와 글자단위(공백 포함) 일치
        assertThat(ExcelExportService.rounddownLabel(1)).isEqualTo("");
        assertThat(ExcelExportService.rounddownLabel(10)).isEqualTo("십원단위 절사");
        assertThat(ExcelExportService.rounddownLabel(100)).isEqualTo("백원단위 절사");
        assertThat(ExcelExportService.rounddownLabel(1000)).isEqualTo("천단위 절사");
        assertThat(ExcelExportService.rounddownLabel(10000)).isEqualTo("만단위 절사");
    }

    // ---------- T4: rounddownLabel invalid ----------
    @Test
    void rounddownLabel_throwsForInvalidInput() {
        assertThatThrownBy(() -> ExcelExportService.rounddownLabel(500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rounddownUnit");
    }

    // ---------- buildRemarkCellValue (FR-9 규칙) ----------
    @Test
    void buildRemarkCellValue_allFourCombinationsPerFR9() {
        // bidActive + unit>1 → 둘 다 표시
        assertThat(ExcelExportService.buildRemarkCellValue(0.91, 1000))
                .isEqualTo("낙찰율 적용 91%\n천단위 절사");
        // bidActive + unit==1 → 낙찰율만
        assertThat(ExcelExportService.buildRemarkCellValue(0.91, 1))
                .isEqualTo("낙찰율 적용 91%");
        // bid 100% + unit>1 → 라벨만
        assertThat(ExcelExportService.buildRemarkCellValue(1.0, 1000))
                .isEqualTo("천단위 절사");
        // bid 100% + unit==1 → 빈 문자열
        assertThat(ExcelExportService.buildRemarkCellValue(1.0, 1)).isEmpty();
        // bid 0 + unit==1 → 빈 문자열
        assertThat(ExcelExportService.buildRemarkCellValue(0.0, 1)).isEmpty();
    }

    @Test
    void buildRemarkCellValue_85percentAnd10000Unit() {
        assertThat(ExcelExportService.buildRemarkCellValue(0.85, 10000))
                .isEqualTo("낙찰율 적용 85%\n만단위 절사");
    }
}
