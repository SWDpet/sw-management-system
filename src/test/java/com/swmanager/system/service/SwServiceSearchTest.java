package com.swmanager.system.service;

import com.swmanager.system.domain.SwProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwService.search 신규 오버로드 통합 검증 (live DB 필요)
 *
 * 기획서 FR/NFR 매핑:
 *   - FR-1 : & 토큰 제거
 *   - FR-9 : 레거시 kw 호환
 *   - FR-10: city 없으면 district 무시
 *   - NFR-7: year 무효 입력 무시
 *
 * 순수 정규화 유틸 검증은 SwServiceNormalizeTest 참조 (DB 불필요, 항상 실행).
 * 본 클래스는 RUN_DB_TESTS=true 환경변수가 있을 때만 실행됨.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true",
        disabledReason = "Live DB required; set RUN_DB_TESTS=true to run. 기본 CI에서는 스킵.")
class SwServiceSearchTest {

    @Autowired
    SwService swService;

    private Pageable pg() { return PageRequest.of(0, 10); }

    @Test
    void kwOnly_returnsResults() {
        Page<SwProject> p = swService.search("UPIS", null, null, null, null, pg());
        assertThat(p).isNotNull();
        assertThat(p.getContent()).isNotNull();
    }

    @Test
    void yearInvalidString_ignored() {
        // NFR-7: parseYearSafe("abc") == null → 400 안 나고 전체 조회와 동일
        Page<SwProject> badYear = swService.search(null, "abc", null, null, null, pg());
        Page<SwProject> noFilter = swService.search(null, null, null, null, null, pg());
        assertThat(badYear).isNotNull();
        assertThat(badYear.getTotalElements()).isEqualTo(noFilter.getTotalElements());
    }

    @Test
    void yearOutOfRange_ignored() {
        // 2000 ≤ year ≤ 2099 위반
        Page<SwProject> p1 = swService.search(null, "1800", null, null, null, pg());
        Page<SwProject> p2 = swService.search(null, "3000", null, null, null, pg());
        Page<SwProject> p0 = swService.search(null, null, null, null, null, pg());
        assertThat(p1.getTotalElements()).isEqualTo(p0.getTotalElements());
        assertThat(p2.getTotalElements()).isEqualTo(p0.getTotalElements());
    }

    @Test
    void districtWithoutCity_ignored() {
        // FR-10
        Page<SwProject> withDistOnly = swService.search(null, null, null, "광명시", null, pg());
        Page<SwProject> noFilter = swService.search(null, null, null, null, null, pg());
        assertThat(withDistOnly.getTotalElements()).isEqualTo(noFilter.getTotalElements());
    }

    @Test
    void fourFilterAnd_noError() {
        Page<SwProject> p = swService.search(null, "2026", "경기도", null, "UPIS", pg());
        assertThat(p).isNotNull();
    }

    @Test
    void legacyAndToken_asLiteral() {
        // T9: 구 &토큰 "2024&UPIS" → 전체 문자열 매칭 (아마 0건이지만 에러 없이)
        Page<SwProject> p = swService.search("2024&UPIS", null, null, null, null, pg());
        assertThat(p).isNotNull();
    }

    @Test
    void legacyOverload_delegates() {
        // FR-9: 기존 search(kw, Pageable) 시그니처 호환
        Page<SwProject> p = swService.search("UPIS", pg());
        assertThat(p).isNotNull();
    }

    @Test
    void whitespaceKw_treatedAsEmpty() {
        // T14
        Page<SwProject> ws = swService.search("   ", null, null, null, null, pg());
        Page<SwProject> nf = swService.search(null, null, null, null, null, pg());
        assertThat(ws.getTotalElements()).isEqualTo(nf.getTotalElements());
    }

    @Test
    void longKw_truncatedTo100Chars() {
        // T18: kw 길이 > 100자 → 100자까지 잘림
        //   - (a) 정규화 직접 검증: 실제 100자 길이로 잘렸는가?
        //   - (b) search 호출 시 예외 없이 응답?
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('a');
        String longKw = sb.toString();

        // (a) 내부 유틸 직접 검증
        String truncated = SwService.normalize(longKw);
        assertThat(truncated).isNotNull();
        assertThat(truncated.length()).isEqualTo(SwService.KW_MAX_LEN);
        assertThat(truncated.length()).isEqualTo(100);

        // (b) end-to-end 검색 호출 시 에러 없이 응답
        Page<SwProject> p = swService.search(longKw, null, null, null, null, pg());
        assertThat(p).isNotNull();
    }

    @Test
    void kwExactly100Chars_notTruncated() {
        // 100자 딱 입력 시 잘리지 않아야 함 (경계)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append('b');
        String kw = sb.toString();
        String normalized = SwService.normalize(kw);
        assertThat(normalized).isNotNull();
        assertThat(normalized.length()).isEqualTo(100);
        assertThat(normalized).isEqualTo(kw);
    }

    @Test
    void parseYearSafe_various() {
        // NFR-7 경계 케이스
        assertThat(SwService.parseYearSafe(null)).isNull();
        assertThat(SwService.parseYearSafe("")).isNull();
        assertThat(SwService.parseYearSafe("abc")).isNull();
        assertThat(SwService.parseYearSafe("1999")).isNull();  // 범위 밖
        assertThat(SwService.parseYearSafe("2100")).isNull();  // 범위 밖
        assertThat(SwService.parseYearSafe("2026")).isEqualTo(2026);
        assertThat(SwService.parseYearSafe(" 2026 ")).isEqualTo(2026);  // trim
    }

    @Test
    void normalizeUpper_converts() {
        assertThat(SwService.normalizeUpper("upis")).isEqualTo("UPIS");
        assertThat(SwService.normalizeUpper(" UPIS ")).isEqualTo("UPIS");
        assertThat(SwService.normalizeUpper(null)).isNull();
        assertThat(SwService.normalizeUpper("")).isNull();
    }
}
