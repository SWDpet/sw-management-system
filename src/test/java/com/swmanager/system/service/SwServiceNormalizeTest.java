package com.swmanager.system.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwService 정규화 유틸 단위 테스트 (Spring 컨텍스트 불필요)
 *
 * 기획서/개발계획서 FR/NFR 매핑:
 *  - NFR-5: trim → 빈값 null
 *  - NFR-7: year 숫자 파싱 + 범위 검증
 *  - T18  : kw 100자 제한
 */
class SwServiceNormalizeTest {

    @Test
    void normalize_trimAndEmpty() {
        assertThat(SwService.normalize(null)).isNull();
        assertThat(SwService.normalize("")).isNull();
        assertThat(SwService.normalize("   ")).isNull();
        assertThat(SwService.normalize("  abc  ")).isEqualTo("abc");
        assertThat(SwService.normalize("hello")).isEqualTo("hello");
    }

    @Test
    void normalize_truncate100Chars() {
        // T18: kw 길이 > 100자 → 정확히 100자로 잘림
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('a');
        String input = sb.toString();

        String out = SwService.normalize(input);
        assertThat(out).isNotNull();
        assertThat(out.length()).isEqualTo(SwService.KW_MAX_LEN);
        assertThat(out.length()).isEqualTo(100);
        assertThat(SwService.KW_MAX_LEN).isEqualTo(100);
    }

    @Test
    void normalize_exactly100_notTruncated() {
        // 경계: 100자 정확히 → 잘리지 않음
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append('b');
        String exact100 = sb.toString();

        String out = SwService.normalize(exact100);
        assertThat(out).isEqualTo(exact100);
        assertThat(out.length()).isEqualTo(100);
    }

    @Test
    void normalize_99Chars_notTruncated() {
        String s99 = "c".repeat(99);
        assertThat(SwService.normalize(s99)).isEqualTo(s99);
    }

    @Test
    void normalize_101Chars_truncated() {
        String s101 = "d".repeat(101);
        String out = SwService.normalize(s101);
        assertThat(out.length()).isEqualTo(100);
    }

    @Test
    void parseYearSafe_validInRange() {
        assertThat(SwService.parseYearSafe("2026")).isEqualTo(2026);
        assertThat(SwService.parseYearSafe(" 2026 ")).isEqualTo(2026);
        assertThat(SwService.parseYearSafe("2000")).isEqualTo(2000);
        assertThat(SwService.parseYearSafe("2099")).isEqualTo(2099);
    }

    @Test
    void parseYearSafe_invalidIgnored() {
        // NFR-7: 무효 입력 모두 null (예외 안 던짐)
        assertThat(SwService.parseYearSafe(null)).isNull();
        assertThat(SwService.parseYearSafe("")).isNull();
        assertThat(SwService.parseYearSafe("   ")).isNull();
        assertThat(SwService.parseYearSafe("abc")).isNull();
        assertThat(SwService.parseYearSafe("20.26")).isNull();
        assertThat(SwService.parseYearSafe("1999")).isNull();  // 범위 밖 아래
        assertThat(SwService.parseYearSafe("2100")).isNull();  // 범위 밖 위
        assertThat(SwService.parseYearSafe("99999")).isNull();
    }

    @Test
    void normalizeUpper_converts() {
        assertThat(SwService.normalizeUpper("upis")).isEqualTo("UPIS");
        assertThat(SwService.normalizeUpper("Upis")).isEqualTo("UPIS");
        assertThat(SwService.normalizeUpper(" upis ")).isEqualTo("UPIS");
        assertThat(SwService.normalizeUpper(null)).isNull();
        assertThat(SwService.normalizeUpper("")).isNull();
        assertThat(SwService.normalizeUpper("   ")).isNull();
    }
}
