package com.swmanager.system.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SensitiveMask 단위 테스트 (PIT 보강 — PII 마스킹 출력 형식 직접 검증).
 *
 * 기존엔 MaskingDetectorTest 가 sensitiveMask 를 *입력 생성용*으로만 호출해 마스킹 출력
 * 형식 자체는 거의 미검증(뮤테이션 29%). 본 테스트로 각 메서드의 정상/포맷불일치 fallback/
 * null·empty/토큰경계 출력을 고정한다(보안 회귀 방지).
 */
class SensitiveMaskTest {

    private final SensitiveMask m = new SensitiveMask();

    // ── ssn: 901201-1234567 → 901201-1****** ──
    @Test void ssn_valid_masksLast6() {
        assertThat(m.ssn("901201-1234567")).isEqualTo("901201-1******");
        assertThat(m.ssn(" 901201-1234567 ")).isEqualTo("901201-1******");  // trim
    }
    @Test void ssn_invalidFormat_safeFallback() {
        assertThat(m.ssn("badformat")).isEqualTo("******");
        assertThat(m.ssn("901201-123")).isEqualTo("******");  // 자리수 불일치
    }
    @Test void ssn_nullOrEmpty_returnsEmpty() {
        assertThat(m.ssn(null)).isEmpty();
        assertThat(m.ssn("   ")).isEmpty();
    }

    // ── tel: 010-1234-5678 → 010-****-5678 ──
    @Test void tel_valid_masksMiddle() {
        assertThat(m.tel("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(m.tel("02-123-4567")).isEqualTo("02-****-4567");   // 2-3-4
        assertThat(m.tel("0504-1234-5678")).isEqualTo("0504-****-5678"); // 4-4-4
    }
    @Test void tel_invalidFormat_safeFallback() {
        assertThat(m.tel("123")).isEqualTo("***-****-****");
        assertThat(m.tel("01012345678")).isEqualTo("***-****-****");  // 하이픈 없음
    }
    @Test void tel_nullOrEmpty_returnsEmpty() {
        assertThat(m.tel(null)).isEmpty();
        assertThat(m.tel("")).isEmpty();
    }
    @Test void mobile_delegatesToTel() {
        assertThat(m.mobile("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(m.mobile("bad")).isEqualTo("***-****-****");
    }

    // ── email: hyeon@domain.com → h***@domain.com ──
    @Test void email_valid_masksLocalAfterFirstChar() {
        assertThat(m.email("hyeon@domain.com")).isEqualTo("h***@domain.com");
        assertThat(m.email("a@b.co")).isEqualTo("a***@b.co");
    }
    @Test void email_noAt_safeFallback() {
        assertThat(m.email("noatsign")).isEqualTo("***@***");
        assertThat(m.email("@startat")).isEqualTo("***@***");   // at==0
        assertThat(m.email("endat@")).isEqualTo("***@***");      // at==length-1
    }
    @Test void email_nullOrEmpty_returnsEmpty() {
        assertThat(m.email(null)).isEmpty();
        assertThat(m.email("")).isEmpty();
    }

    // ── address: 앞 2토큰 유지 + **** ──
    @Test void address_multiToken_keepsFirstTwo() {
        assertThat(m.address("서울 강남구 테헤란로 123")).isEqualTo("서울 강남구 ****");
    }
    @Test void address_exactlyTwoTokens() {
        assertThat(m.address("서울 강남구")).isEqualTo("서울 강남구 ****");
    }
    @Test void address_singleToken_fullMask() {
        assertThat(m.address("서울강남구테헤란로123")).isEqualTo("****");
    }
    @Test void address_nullOrEmpty_returnsEmpty() {
        assertThat(m.address(null)).isEmpty();
        assertThat(m.address("   ")).isEmpty();
    }
}
