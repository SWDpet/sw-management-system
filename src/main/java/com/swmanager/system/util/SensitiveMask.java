package com.swmanager.system.util;

import org.springframework.stereotype.Component;

/**
 * 민감정보 마스킹 유틸.
 *
 * 스프린트 6 (2026-04-19) — 관리자 사용자 통합 관리 페이지에서 서버 렌더 시
 * 평문 PII 가 HTML 에 포함되지 않도록 하는 단일 소스.
 *
 * Thymeleaf 3.1 에서 T(...) 정적 호출이 금지되므로 Spring Bean 으로 등록하여
 * ${@sensitiveMask.tel(...)} 형태로 호출 가능하게 함.
 *
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 사용처별 정책 (S3-B users-masking-regression-fix, 2026-04-20):
 *
 *  • DB 저장        : 항상 unmasked 원본 (마스킹 값 저장 금지)
 *  • 마이페이지     : unmasked (본인 전용 — 본인 정보를 본인이 확인·수정하는 화면이므로 마스킹하지 않음)
 *  • 관리자 사용자 목록 등 일반 화면: 마스킹 표시 (호출 측에서 명시적 사용)
 *  • 문서관리·견적서·견적서 PDF·실문서: unmasked (마스킹 호출 금지)
 *  • 로그/디버그   : 마스킹 표시 (값 절대 미포함, userid + 필드명만 기록)
 *
 * 회귀 방지: 마스킹된 값이 폼 submit 으로 DB 저장되는 회귀를
 *   {@link MaskingDetector} + MyPageController 가드 로 차단 (S3-B).
 *
 * 자세한 정책: docs/DESIGN.md §마스킹 정책 + docs/product-specs/users-masking-regression-fix.md
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * 정책 (마스킹 동작):
 *   - null/empty 입력 → 빈 문자열 반환
 *   - 포맷이 예상과 다를 경우에도 **원본 반환 금지** (FR-3-A).
 *     안전 기본 마스킹("***" 또는 "첫자 + ***") 로 대체.
 */
@Component("sensitiveMask")
public class SensitiveMask {

    /** 주민번호: 901201-1234567 → 901201-1****** (뒤 6자리 마스킹) */
    public String ssn(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        // 정규: 6자리-7자리
        if (trimmed.matches("\\d{6}-\\d{7}")) {
            return trimmed.substring(0, 8) + "******";
        }
        // 포맷 불일치 — 안전 기본 마스킹
        return "******";
    }

    /** 전화번호: 010-1234-5678 → 010-****-5678 */
    public String tel(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        // 3-4-4 또는 2-4-4 또는 3-3-4 패턴
        if (trimmed.matches("\\d{2,4}-\\d{3,4}-\\d{4}")) {
            int first = trimmed.indexOf('-');
            int last = trimmed.lastIndexOf('-');
            if (first > 0 && last > first) {
                return trimmed.substring(0, first + 1) + "****" + trimmed.substring(last);
            }
        }
        // 포맷 불일치 → 길이 관계없이 ***로 대체
        return "***-****-****";
    }

    /** 휴대전화 — tel 과 동일 마스킹 적용 */
    public String mobile(String s) {
        return tel(s);
    }

    /** 이메일: hyeon@domain.com → h***@domain.com */
    public String email(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        int at = trimmed.indexOf('@');
        if (at > 0 && at < trimmed.length() - 1) {
            String local = trimmed.substring(0, at);
            String domain = trimmed.substring(at);
            String first = local.substring(0, 1);
            return first + "***" + domain;
        }
        // 포맷 불일치 (@ 없음 등) — 안전 기본 마스킹
        return "***@***";
    }

    /** 주소: "서울 강남구 테헤란로 123" → "서울 강남구 ****" (앞 2 토큰까지만).
     *  공백 없는 단일 토큰(예: "서울강남구테헤란로123") 은 원문 보호 위해 전체 마스킹. */
    public String address(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        String[] tokens = trimmed.split("\\s+", 3);
        if (tokens.length >= 2) {
            return tokens[0] + " " + tokens[1] + " ****";
        }
        // 단일 토큰(공백 없음) — 원본 노출 금지. 안전 기본 마스킹.
        return "****";
    }
}
