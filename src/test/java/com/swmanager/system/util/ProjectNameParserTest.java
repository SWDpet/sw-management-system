package com.swmanager.system.util;

import com.swmanager.system.domain.SysMst;
import com.swmanager.system.repository.SysMstRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProjectNameParser 단위 테스트 — T10~T13.
 *
 * Sprint: pdf-contract-autofill (PCA-1)
 * 근거: docs/exec-plans/pdf-contract-autofill.md §6-2
 */
class ProjectNameParserTest {

    private SysMstRepository sysMstRepository;
    private ProjectNameParser parser;

    @BeforeEach
    void setUp() {
        sysMstRepository = mock(SysMstRepository.class);

        // sys_mst 가짜 데이터
        SysMst upis = new SysMst();
        upis.setCd("UPIS");
        upis.setNm("도시계획정보체계");
        when(sysMstRepository.findById("UPIS")).thenReturn(Optional.of(upis));

        SysMst kras = new SysMst();
        kras.setCd("KRAS");
        kras.setNm("부동산종합공부시스템");
        when(sysMstRepository.findById("KRAS")).thenReturn(Optional.of(kras));

        // XYZ 는 미매칭
        when(sysMstRepository.findById("XYZ")).thenReturn(Optional.empty());

        parser = new ProjectNameParser(sysMstRepository);
    }

    @Test
    @DisplayName("T10: 양양군 UPIS 유지보수 → 도시계획정보체계 / UPIS / 4(유상)")
    void t10_yangyang_upis() {
        ProjectNameParser.ParseResult result = parser.parse(
            "2026년 양양군 도시계획정보체계(UPIS) 유지보수 용역"
        );
        assertEquals("도시계획정보체계", result.sysNm());
        assertEquals("UPIS", result.sysNmEn());
        assertEquals("4", result.bizTypeCd());
    }

    @Test
    @DisplayName("T11: KRAS 유지보수 → 부동산종합공부시스템 / KRAS / 4(유상)")
    void t11_kras_maintenance() {
        ProjectNameParser.ParseResult result = parser.parse(
            "OO시 부동산종합공부시스템(KRAS) 유지보수"
        );
        assertEquals("부동산종합공부시스템", result.sysNm());
        assertEquals("KRAS", result.sysNmEn());
        assertEquals("4", result.bizTypeCd());
    }

    @Test
    @DisplayName("T12: 괄호 영문 없음 + 유지보수 키워드 없음 → 모두 null")
    void t12_no_paren_no_keyword() {
        ProjectNameParser.ParseResult result = parser.parse(
            "OO 신규 구축 용역"
        );
        assertNull(result.sysNm());
        assertNull(result.sysNmEn());
        assertNull(result.bizTypeCd());
    }

    @Test
    @DisplayName("T13: 괄호 있지만 sys_mst 미매칭 + 유지보수 → null/null/4")
    void t13_unmatched_paren_with_maintenance() {
        ProjectNameParser.ParseResult result = parser.parse(
            "OO (XYZ) 유지보수"
        );
        assertNull(result.sysNm());
        assertNull(result.sysNmEn());
        assertEquals("4", result.bizTypeCd());
    }

    // === Edge cases ===

    @Test
    @DisplayName("Edge-1: 빈 입력 → 모두 null")
    void edge1_blank() {
        ProjectNameParser.ParseResult result = parser.parse("");
        assertNull(result.sysNm());
        assertNull(result.sysNmEn());
        assertNull(result.bizTypeCd());
    }

    @Test
    @DisplayName("Edge-2: null 입력 → 모두 null (NPE 없음)")
    void edge2_null() {
        ProjectNameParser.ParseResult result = parser.parse(null);
        assertNull(result.sysNm());
        assertNull(result.sysNmEn());
        assertNull(result.bizTypeCd());
    }

    @Test
    @DisplayName("Edge-3: 괄호 안이 소문자 — 매칭 안 함 (대문자 패턴만 인정)")
    void edge3_lowercase_paren() {
        ProjectNameParser.ParseResult result = parser.parse(
            "OO 시스템(upis) 유지보수"
        );
        assertNull(result.sysNm());
        assertNull(result.sysNmEn());
        assertEquals("4", result.bizTypeCd());
    }

    @Test
    @DisplayName("Edge-4: 1글자 영문은 매칭 X (2자 이상만)")
    void edge4_single_letter() {
        ProjectNameParser.ParseResult result = parser.parse(
            "OO 시스템(A) 유지보수"
        );
        assertNull(result.sysNm());
        assertNull(result.sysNmEn());
        assertEquals("4", result.bizTypeCd());
    }
}
