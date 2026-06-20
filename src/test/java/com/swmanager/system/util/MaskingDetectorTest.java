package com.swmanager.system.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MaskingDetector 단위 테스트 (S3-B users-masking-regression-fix).
 *
 * 검증 매트릭스:
 *  T1 tel 정상 입력 → false
 *  T2 tel 마스킹 패턴 (010-****-1234) → true
 *  T3 tel FR-8 동등 비교 (SensitiveMask(currentDb) == input) → true
 *  T4 email 정상 → false
 *  T5 email 마스킹 (u***@x.com) → true
 *  T6 ssn 마스킹 (901201-1******) → true
 *  T7 address 정상 → false
 *  T8 address 마스킹 (*** 강남구) → true (보수 정규식)
 *  T9 null/empty 입력 → false (NPE 없음)
 */
class MaskingDetectorTest {

    MaskingDetector detector;
    SensitiveMask sensitiveMask;

    @BeforeEach
    void setUp() {
        sensitiveMask = new SensitiveMask();
        detector = new MaskingDetector();
        // @Autowired 대신 reflection 으로 주입
        try {
            java.lang.reflect.Field f = MaskingDetector.class.getDeclaredField("sensitiveMask");
            f.setAccessible(true);
            f.set(detector, sensitiveMask);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── tel ──────────────────────────────────────
    @Test void T1_telNormal_returnsFalse() {
        assertThat(detector.isMaskedTel("070-7113-8093", "010-1234-5678")).isFalse();
    }

    @Test void T2_telMaskedPattern_returnsTrue() {
        assertThat(detector.isMaskedTel("010-****-1234", null)).isTrue();
        assertThat(detector.isMaskedTel("070-****-9805", null)).isTrue();
    }

    @Test void T3_telEqualsMaskedDbValue_returnsTrue() {
        // SensitiveMask.tel("010-1234-5678") = "010-****-5678" 라고 가정 → input 과 동등
        String dbValue = "010-1234-5678";
        String maskedDb = sensitiveMask.tel(dbValue);  // "010-****-5678"
        assertThat(detector.isMaskedTel(maskedDb, dbValue)).isTrue();
    }

    /** 동등분기 단독 검증(뮤테이션): 포맷불일치 마스크 "***-****-****" 는 TEL_MASK 정규식 불일치 →
     *  1차 동등비교 분기만이 true 를 만든다(L39 negated conditional kill). */
    @Test void T3b_telEqualsMaskedDbValue_nonRegexMask_returnsTrue() {
        String dbValue = "비전화포맷";                 // tel() 포맷 불일치 → "***-****-****"
        String maskedDb = sensitiveMask.tel(dbValue);   // "***-****-****"
        assertThat(detector.isMaskedTel(maskedDb, null)).isFalse();  // 정규식만으론 false 확인
        assertThat(detector.isMaskedTel(maskedDb, dbValue)).isTrue();  // 동등분기로만 true
    }

    // ─── email ──────────────────────────────────────
    @Test void T4_emailNormal_returnsFalse() {
        assertThat(detector.isMaskedEmail("ukjin914@uitgis.com", "old@x.com")).isFalse();
    }

    @Test void T5_emailMaskedPattern_returnsTrue() {
        assertThat(detector.isMaskedEmail("u***@uitgis.com", null)).isTrue();
        assertThat(detector.isMaskedEmail("h***@domain.com", null)).isTrue();
    }

    /** 동등분기 단독 검증(뮤테이션): @ 없는 입력의 마스크 "***@***" 는 EMAIL_MASK 불일치 →
     *  1차 동등비교 분기만이 true(L46 negated conditional kill). */
    @Test void T5b_emailEqualsMaskedDbValue_nonRegexMask_returnsTrue() {
        String dbValue = "골뱅이없음";                  // email() @ 없음 → "***@***"
        String maskedDb = sensitiveMask.email(dbValue); // "***@***"
        assertThat(detector.isMaskedEmail(maskedDb, null)).isFalse();   // 정규식만으론 false
        assertThat(detector.isMaskedEmail(maskedDb, dbValue)).isTrue(); // 동등분기로만 true
    }

    // ─── ssn ──────────────────────────────────────
    @Test void T6_ssnMaskedPattern_returnsTrue() {
        assertThat(detector.isMaskedSsn("901201-1******", null)).isTrue();
    }

    @Test void T6b_ssnNormal_returnsFalse() {
        assertThat(detector.isMaskedSsn("901201-1234567", null)).isFalse();
    }

    /** 동등분기 단독 검증(뮤테이션): 포맷불일치 마스크 "******" 는 SSN_MASK 불일치 →
     *  1차 동등비교 분기만이 true(L52 negated conditional kill). */
    @Test void T6c_ssnEqualsMaskedDbValue_nonRegexMask_returnsTrue() {
        String dbValue = "주민번호아님";                // ssn() 포맷 불일치 → "******"
        String maskedDb = sensitiveMask.ssn(dbValue);   // "******"
        assertThat(detector.isMaskedSsn(maskedDb, null)).isFalse();   // 정규식만으론 false
        assertThat(detector.isMaskedSsn(maskedDb, dbValue)).isTrue(); // 동등분기로만 true
    }

    // ─── address ──────────────────────────────────────
    @Test void T7_addressNormal_returnsFalse() {
        assertThat(detector.isMaskedAddress("서울 강남구 테헤란로 123", null)).isFalse();
    }

    @Test void T8_addressMaskedTokens_returnsTrue() {
        assertThat(detector.isMaskedAddress("서울 *** 테헤란로 ***", null)).isTrue();
    }

    /** 동등분기 음성 검증(뮤테이션): 평문 입력 ≠ 마스킹된 DB값, 별표 없음 →
     *  동등분기·정규식 모두 false 여야 한다(L59 equals-negation kill 시도). */
    @Test void T8b_addressUnmaskedNotEqualMaskedDb_returnsFalse() {
        // address("서울 강남구 테헤란로 999") = "서울 강남구 ****" ≠ input("...123")
        assertThat(detector.isMaskedAddress("서울 강남구 테헤란로 123", "서울 강남구 테헤란로 999")).isFalse();
    }

    // ─── null/empty ──────────────────────────────────────
    @Test void T9_nullInput_returnsFalse_noNpe() {
        assertThat(detector.isMaskedTel(null, "070-1234-5678")).isFalse();
        assertThat(detector.isMaskedEmail(null, "x@y.com")).isFalse();
        assertThat(detector.isMaskedSsn(null, "901201-1234567")).isFalse();
        assertThat(detector.isMaskedAddress(null, "서울")).isFalse();
    }

    @Test void T9b_emptyInput_returnsFalse() {
        assertThat(detector.isMaskedTel("", "010-1234-5678")).isFalse();
        assertThat(detector.isMaskedEmail("", "x@y.com")).isFalse();
        // address 빈 입력 + null db: address(null)="" 와 동등이 되어선 안 됨(L59 !=null 분기 kill)
        assertThat(detector.isMaskedAddress("", null)).isFalse();
        assertThat(detector.isMaskedSsn("", null)).isFalse();
    }
}
