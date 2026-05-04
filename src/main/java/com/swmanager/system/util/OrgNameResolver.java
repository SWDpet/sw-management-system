package com.swmanager.system.util;

import org.springframework.stereotype.Component;

/**
 * 행정구역(시도/시군구) → 기관명·기관장명 변환 유틸.
 *
 * Sprint: pdf-contract-autofill (PCA-1, 2026-05-04)
 * 근거: docs/product-specs/pdf-contract-autofill.md §4-1 (사용자 확정 9 케이스)
 *
 * 변환 규칙:
 *  • 시군구 없음 (광역 자체 발주):
 *      - 도/특별자치도:           cityNm + "청"  /  cityNm + "지사"   (예: 강원특별자치도청 / 강원특별자치도지사)
 *      - 특별자치시/특별시/광역시: cityNm + "청"  /  cityNm + "장"     (예: 서울특별시청 / 서울특별시장)
 *  • 도/특별자치도 산하:
 *      - 시 → distNm + "청"  /  distNm + "장"     (예: 강릉시청 / 강릉시장)
 *      - 군 → distNm + "청"  /  distNm + "수"     (예: 양양군청 / 양양군수)
 *  • 특별시/광역시 산하 (시도명 prefix 포함):
 *      - 구 → "{cityNm} {distNm}청"  /  "{cityNm} {distNm}청장"   (예: 서울특별시 강남구청 / 서울특별시 강남구청장)
 *      - 군 → "{cityNm} {distNm}청"  /  "{cityNm} {distNm}수"     (예: 인천광역시 강화군청 / 인천광역시 강화군수)
 *
 * 매핑 실패 시 null 반환 → 호출 측에서 사용자 수동 입력 유도 (FR-9).
 *
 * 단위 테스트: OrgNameResolverTest (T1~T9, 9 케이스 모두 PASS 보장).
 */
@Component
public class OrgNameResolver {

    /** 변환 결과: (기관명, 기관장명) */
    public record OrgInfo(String orgNm, String orgLghNm) {}

    /**
     * 시도·시군구 입력 → 기관명·기관장명 변환.
     *
     * @param cityNm 시도명 (예: "강원특별자치도", "서울특별시", "울산광역시")
     * @param distNm 시군구명 (예: "양양군", "강남구"). null/blank 이면 광역 자체 발주.
     * @return OrgInfo 또는 매핑 실패 시 null
     */
    public OrgInfo resolve(String cityNm, String distNm) {
        if (cityNm == null || cityNm.isBlank()) {
            return null;
        }

        boolean isDo = cityNm.endsWith("도");
        boolean isCityArea = cityNm.endsWith("특별시") || cityNm.endsWith("광역시");
        boolean hasDist = distNm != null && !distNm.isBlank();

        // [1] 시군구 없음 — 광역 자체 발주
        if (!hasDist) {
            if (isDo) {
                return new OrgInfo(cityNm + "청", cityNm + "지사");
            }
            // 특별자치시·특별시·광역시
            return new OrgInfo(cityNm + "청", cityNm + "장");
        }

        // [2] 도/특별자치도 산하 시·군 (시도명 prefix 없음)
        if (isDo) {
            String suffix = distNm.endsWith("군") ? "수" : "장";
            return new OrgInfo(distNm + "청", distNm + suffix);
        }

        // [3] 특별시/광역시 산하 구·군 (시도명 prefix 포함)
        if (isCityArea) {
            String prefix = cityNm + " " + distNm;
            String head = distNm.endsWith("군") ? "수" : "청장";
            return new OrgInfo(prefix + "청", prefix + head);
        }

        // 매핑 실패 — 사용자 수동 입력 유도
        return null;
    }
}
