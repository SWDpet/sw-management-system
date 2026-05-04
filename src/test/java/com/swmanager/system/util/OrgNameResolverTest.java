package com.swmanager.system.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrgNameResolver 단위 테스트 — T1~T9 (9 케이스).
 *
 * Sprint: pdf-contract-autofill (PCA-1)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §6-1 (기획서 사용자 확정 9 케이스)
 */
class OrgNameResolverTest {

    private final OrgNameResolver resolver = new OrgNameResolver();

    @Test
    @DisplayName("T1: 도/특별자치도 자체 발주 — 강원특별자치도청 / 강원특별자치도지사")
    void t1_dojachido_self() {
        OrgNameResolver.OrgInfo result = resolver.resolve("강원특별자치도", null);
        assertNotNull(result);
        assertEquals("강원특별자치도청", result.orgNm());
        assertEquals("강원특별자치도지사", result.orgLghNm());
    }

    @Test
    @DisplayName("T2: 특별자치시 자체 발주 — 세종특별자치시청 / 세종특별자치시장")
    void t2_sejong_self() {
        OrgNameResolver.OrgInfo result = resolver.resolve("세종특별자치시", null);
        assertNotNull(result);
        assertEquals("세종특별자치시청", result.orgNm());
        assertEquals("세종특별자치시장", result.orgLghNm());
    }

    @Test
    @DisplayName("T3: 특별시 자체 발주 — 서울특별시청 / 서울특별시장")
    void t3_seoul_self() {
        OrgNameResolver.OrgInfo result = resolver.resolve("서울특별시", null);
        assertNotNull(result);
        assertEquals("서울특별시청", result.orgNm());
        assertEquals("서울특별시장", result.orgLghNm());
    }

    @Test
    @DisplayName("T4: 광역시 자체 발주 — 울산광역시청 / 울산광역시장")
    void t4_gwangyeoksi_self() {
        OrgNameResolver.OrgInfo result = resolver.resolve("울산광역시", null);
        assertNotNull(result);
        assertEquals("울산광역시청", result.orgNm());
        assertEquals("울산광역시장", result.orgLghNm());
    }

    @Test
    @DisplayName("T5: 도 산하 시 — 강릉시청 / 강릉시장 (시도명 prefix 없음)")
    void t5_do_si() {
        OrgNameResolver.OrgInfo result = resolver.resolve("강원특별자치도", "강릉시");
        assertNotNull(result);
        assertEquals("강릉시청", result.orgNm());
        assertEquals("강릉시장", result.orgLghNm());
    }

    @Test
    @DisplayName("T6: 도 산하 군 — 양양군청 / 양양군수 (시도명 prefix 없음)")
    void t6_do_gun() {
        OrgNameResolver.OrgInfo result = resolver.resolve("강원특별자치도", "양양군");
        assertNotNull(result);
        assertEquals("양양군청", result.orgNm());
        assertEquals("양양군수", result.orgLghNm());
    }

    @Test
    @DisplayName("T7: 특별시 산하 구 — 서울특별시 강남구청 / 서울특별시 강남구청장")
    void t7_teukbyeolsi_gu() {
        OrgNameResolver.OrgInfo result = resolver.resolve("서울특별시", "강남구");
        assertNotNull(result);
        assertEquals("서울특별시 강남구청", result.orgNm());
        assertEquals("서울특별시 강남구청장", result.orgLghNm());
    }

    @Test
    @DisplayName("T8: 광역시 산하 구 — 울산광역시 북구청 / 울산광역시 북구청장")
    void t8_gwangyeoksi_gu() {
        OrgNameResolver.OrgInfo result = resolver.resolve("울산광역시", "북구");
        assertNotNull(result);
        assertEquals("울산광역시 북구청", result.orgNm());
        assertEquals("울산광역시 북구청장", result.orgLghNm());
    }

    @Test
    @DisplayName("T9: 광역시 산하 군 — 인천광역시 강화군청 / 인천광역시 강화군수")
    void t9_gwangyeoksi_gun() {
        OrgNameResolver.OrgInfo result = resolver.resolve("인천광역시", "강화군");
        assertNotNull(result);
        assertEquals("인천광역시 강화군청", result.orgNm());
        assertEquals("인천광역시 강화군수", result.orgLghNm());
    }

    // === Edge cases ===

    @Test
    @DisplayName("Edge-1: cityNm null 입력 → null 반환")
    void edge1_null_city() {
        assertNull(resolver.resolve(null, "양양군"));
    }

    @Test
    @DisplayName("Edge-2: cityNm 빈 문자열 → null 반환")
    void edge2_blank_city() {
        assertNull(resolver.resolve("", "양양군"));
        assertNull(resolver.resolve("   ", "양양군"));
    }

    @Test
    @DisplayName("Edge-3: distNm 빈 문자열은 자체 발주로 처리")
    void edge3_blank_dist_treated_as_self() {
        OrgNameResolver.OrgInfo result = resolver.resolve("강원특별자치도", "");
        assertNotNull(result);
        assertEquals("강원특별자치도청", result.orgNm());
    }
}
