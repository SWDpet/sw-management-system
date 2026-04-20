package com.swmanager.system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 사용자 입력값에 마스킹 패턴이 포함되어 있는지 감지 (S3-B users-masking-regression-fix).
 *
 * 정책: DB 회귀 방지 — 마이페이지 등 폼 submit 시 마스킹된 값이 DB로
 *       다시 저장되는 현상을 차단.
 *
 * 감지 방식 (오탐 최소화):
 *   1차) FR-8 동등 비교 — SensitiveMask(currentDb) == input → 100% 회귀 확정
 *   2차) 컬럼별 정규식 fallback (tel/mobile/email/ssn/address)
 *
 * 사용처: MyPageController.updateMyInfo() 가드 (S3-B Step 3)
 */
@Component
public class MaskingDetector {

    /** 010-****-5678 형식 */
    private static final Pattern TEL_MASK = Pattern.compile("^\\d{2,4}-\\*{4}-\\d{4}$");
    /** h***@domain.com 형식 (앞 1~5자 후 *** 후 @ 도메인) */
    private static final Pattern EMAIL_MASK = Pattern.compile("^.{1,5}\\*{3,}@.+$");
    /** 901201-1****** 형식 */
    private static final Pattern SSN_MASK = Pattern.compile("^\\d{6}-\\d\\*{6}$");
    /** 보수 fallback (3자 이상 연속 *) */
    private static final Pattern GENERIC_MASK = Pattern.compile("\\*{3,}");

    @Autowired
    private SensitiveMask sensitiveMask;

    /** tel/mobile 통합 (포맷 동일) */
    public boolean isMaskedTel(String input, String currentDbValue) {
        if (input == null) return false;
        // 1차) 동등 비교
        if (currentDbValue != null && input.equals(sensitiveMask.tel(currentDbValue))) return true;
        // 2차) 정규식
        return TEL_MASK.matcher(input).matches();
    }

    public boolean isMaskedEmail(String input, String currentDbValue) {
        if (input == null) return false;
        if (currentDbValue != null && input.equals(sensitiveMask.email(currentDbValue))) return true;
        return EMAIL_MASK.matcher(input).matches();
    }

    public boolean isMaskedSsn(String input, String currentDbValue) {
        if (input == null) return false;
        if (currentDbValue != null && input.equals(sensitiveMask.ssn(currentDbValue))) return true;
        return SSN_MASK.matcher(input).matches();
    }

    public boolean isMaskedAddress(String input, String currentDbValue) {
        if (input == null) return false;
        // address 는 자유 입력 — 동등 비교를 1차로 사용해 오탐 최소화
        if (currentDbValue != null && input.equals(sensitiveMask.address(currentDbValue))) return true;
        // 2차 보수: 3자 이상 *
        return GENERIC_MASK.matcher(input).find();
    }
}
