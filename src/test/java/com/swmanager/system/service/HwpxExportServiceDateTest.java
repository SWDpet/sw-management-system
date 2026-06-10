package com.swmanager.system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 준공계 날짜 "연·월만, 일 칸 비움" 포맷 단위테스트 (completion-yearmonth-date).
 */
class HwpxExportServiceDateTest {

    @Test
    void yearMonth_blankDay_validFormats() {
        assertEquals("2026년 06월    일", HwpxExportService.formatYearMonthBlankDay("2026-06"));
        assertEquals("2026년 06월    일", HwpxExportService.formatYearMonthBlankDay("2026-06-15"));
        assertEquals("2026년 12월    일", HwpxExportService.formatYearMonthBlankDay("2026-12-01"));
    }

    @Test
    void yearMonth_blankDay_emptyOrNull() {
        assertEquals("", HwpxExportService.formatYearMonthBlankDay(null));
        assertEquals("", HwpxExportService.formatYearMonthBlankDay(""));
        assertEquals("", HwpxExportService.formatYearMonthBlankDay("   "));
    }

    @Test
    void yearMonth_blankDay_rejectsMalformed() {
        // 앞 7자만 떼어 통과시키지 않도록 — 형식 외 입력은 공란
        assertEquals("", HwpxExportService.formatYearMonthBlankDay("2026-06-bad"));
        assertEquals("", HwpxExportService.formatYearMonthBlankDay("bad"));
        assertEquals("", HwpxExportService.formatYearMonthBlankDay("2026/06"));
        assertEquals("", HwpxExportService.formatYearMonthBlankDay("2026-13"));   // 13월 → parse 실패
    }
}
