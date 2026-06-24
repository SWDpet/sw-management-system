package com.swmanager.system.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * HwpxExportService 단위테스트 (커버리지 상향 beyond-A) — 순수 static 헬퍼 주요 분기 커버.
 * 한글 금액 변환(내부-0 갭 포함)·수신처 빌더·날짜 포맷 4종. (generateHwpx 의 템플릿 생성은 별도)
 */
class HwpxExportServiceTest {

    // ===== convertToKoreanAmount (금/원 접두접미 없음, 일천/일백/일십 생략 규칙) =====

    @Test
    void convertToKoreanAmount_nullOrZero_returnsYeong() {
        assertThat(HwpxExportService.convertToKoreanAmount(null)).isEqualTo("영");
        assertThat(HwpxExportService.convertToKoreanAmount(0L)).isEqualTo("영");
    }

    @Test
    void convertToKoreanAmount_omitsLeadingOneForThousandHundredTen() {
        assertThat(HwpxExportService.convertToKoreanAmount(1_000L)).isEqualTo("천");   // 일천 아님
        assertThat(HwpxExportService.convertToKoreanAmount(100L)).isEqualTo("백");
        assertThat(HwpxExportService.convertToKoreanAmount(10L)).isEqualTo("십");
        assertThat(HwpxExportService.convertToKoreanAmount(1L)).isEqualTo("일");
    }

    @Test
    void convertToKoreanAmount_keepsDigitWhenGreaterThanOne() {
        assertThat(HwpxExportService.convertToKoreanAmount(2_000L)).isEqualTo("이천");
        assertThat(HwpxExportService.convertToKoreanAmount(20L)).isEqualTo("이십");
    }

    @Test
    void convertToKoreanAmount_multiUnit() {
        // 도큐 예시: 8,778,000 → 팔백칠십칠만팔천
        assertThat(HwpxExportService.convertToKoreanAmount(8_778_000L)).isEqualTo("팔백칠십칠만팔천");
        assertThat(HwpxExportService.convertToKoreanAmount(100_000_000L)).isEqualTo("일억");
    }

    @Test
    void convertToKoreanAmount_internalZeroGaps() {
        // 하위 4자리(part)가 0 → 스킵되는 분기 + 만 단위에 백/십만 존재
        assertThat(HwpxExportService.convertToKoreanAmount(1_000_000L)).isEqualTo("백만");   // 하위 만=0 skip
        assertThat(HwpxExportService.convertToKoreanAmount(10_008L)).isEqualTo("일만팔");     // 천/백/십=0, 일만+팔
    }

    // ===== 수신처 빌더 =====

    @Test
    void buildRecipientHead_appendsGuiha_orEmpty() {
        assertThat(HwpxExportService.buildRecipientHead("강원도청")).isEqualTo("강원도청 귀하");
        assertThat(HwpxExportService.buildRecipientHead(null)).isEmpty();
        assertThat(HwpxExportService.buildRecipientHead("")).isEmpty();
    }

    @Test
    void buildRecipientOrg_passthrough_orEmpty() {
        assertThat(HwpxExportService.buildRecipientOrg("강릉시")).isEqualTo("강릉시");
        assertThat(HwpxExportService.buildRecipientOrg(null)).isEmpty();
        assertThat(HwpxExportService.buildRecipientOrg("")).isEmpty();
    }

    // ===== 날짜 포맷 =====

    @Test
    void formatDate_dotted_orEmptyOnNull() {
        assertThat(HwpxExportService.formatDate(LocalDate.of(2025, 12, 31))).isEqualTo("2025. 12. 31.");
        assertThat(HwpxExportService.formatDate(null)).isEmpty();
    }

    @Test
    void formatDateKorean_andPaddedVariant() {
        assertThat(HwpxExportService.formatDateKorean(LocalDate.of(2025, 12, 31))).isEqualTo("2025년 12월 31일");
        assertThat(HwpxExportService.formatDateKorean(null)).isEmpty();
        // padded: 한자리 월/일 0-패딩
        assertThat(HwpxExportService.formatDateKoreanPadded(LocalDate.of(2026, 1, 9))).isEqualTo("2026년 01월 09일");
        assertThat(HwpxExportService.formatDateKoreanPadded(null)).isEmpty();
    }

    @Test
    void formatYearMonthBlankDay_acceptsYmAndYmd_blanksDay() {
        assertThat(HwpxExportService.formatYearMonthBlankDay("2026-06")).isEqualTo("2026년 06월    일");
        assertThat(HwpxExportService.formatYearMonthBlankDay("2026-06-15")).isEqualTo("2026년 06월    일");
    }

    @Test
    void formatYearMonthBlankDay_invalidOrBlank_returnsEmpty() {
        assertThat(HwpxExportService.formatYearMonthBlankDay(null)).isEmpty();
        assertThat(HwpxExportService.formatYearMonthBlankDay("   ")).isEmpty();
        assertThat(HwpxExportService.formatYearMonthBlankDay("2026-06-bad")).isEmpty();  // 형식 외
        assertThat(HwpxExportService.formatYearMonthBlankDay("not-a-date")).isEmpty();
        assertThat(HwpxExportService.formatYearMonthBlankDay("2026-13")).isEmpty();      // 파싱 실패(13월)
    }
}
