package com.swmanager.system.service;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.dto.WorkPlanDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * workplan-target-infra-cascade — 대상(인프라/지역) 해석 단위테스트.
 * saveWorkPlan 의 FR-9/9b/9c 분기 + 정규화 헬퍼(§5-4).
 */
class WorkPlanTargetTest {

    WorkPlanService service;
    WorkPlanRepository workPlanRepository;
    InfraRepository infraRepository;
    UserRepository userRepository;
    SigunguCodeRepository sigunguCodeRepository;

    @BeforeEach
    void setup() {
        service = new WorkPlanService();
        workPlanRepository = mock(WorkPlanRepository.class);
        infraRepository = mock(InfraRepository.class);
        userRepository = mock(UserRepository.class);
        sigunguCodeRepository = mock(SigunguCodeRepository.class);
        ReflectionTestUtils.setField(service, "workPlanRepository", workPlanRepository);
        ReflectionTestUtils.setField(service, "infraRepository", infraRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "sigunguCodeRepository", sigunguCodeRepository);
        when(workPlanRepository.save(any(WorkPlan.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private WorkPlanDTO baseDto(String planType) {
        WorkPlanDTO d = new WorkPlanDTO();
        d.setPlanType(planType);
        d.setTitle("테스트 업무");
        d.setStartDate("2026-06-11");
        return d;
    }

    private SigunguCode sgg(String code, String sido, String sggNm) {
        SigunguCode s = new SigunguCode();
        s.setAdmSectC(code); s.setSidoNm(sido); s.setSggNm(sggNm);
        return s;
    }

    @Test
    void support_directInput_savesRegion_noInfra() {
        WorkPlanDTO d = baseDto("SUPPORT");
        d.setRegionCode("64800");
        d.setTargetSysNm("도시계획정보체계");
        when(sigunguCodeRepository.findById("64800")).thenReturn(Optional.of(sgg("64800", "경상남도", "경상남도")));

        WorkPlan saved = service.saveWorkPlan(d, null);

        assertNull(saved.getInfra());
        assertEquals("64800", saved.getRegionCode());
        assertEquals("경상남도", saved.getRegionCityNm());
        assertEquals("경상남도", saved.getRegionDistNm());
        assertEquals("도시계획정보체계", saved.getTargetSysNm());
    }

    @Test
    void nonSupport_directInput_rejected() {
        WorkPlanDTO d = baseDto("INSPECT"); // SUPPORT 아님
        d.setRegionCode("41570");
        d.setTargetSysNm("UPIS");
        assertThrows(IllegalArgumentException.class, () -> service.saveWorkPlan(d, null));
    }

    @Test
    void support_invalidRegionCode_rejected() {
        WorkPlanDTO d = baseDto("SUPPORT");
        d.setRegionCode("99999");
        d.setTargetSysNm("UPIS");
        when(sigunguCodeRepository.findById("99999")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.saveWorkPlan(d, null));
    }

    @Test
    void support_blankSysNm_rejected() {
        WorkPlanDTO d = baseDto("SUPPORT");
        d.setRegionCode("64800");
        d.setTargetSysNm("   ");
        when(sigunguCodeRepository.findById("64800")).thenReturn(Optional.of(sgg("64800", "경상남도", "경상남도")));
        assertThrows(IllegalArgumentException.class, () -> service.saveWorkPlan(d, null));
    }

    @Test
    void noTarget_savesNullRegion() {
        WorkPlanDTO d = baseDto("INSPECT"); // 대상 없음
        WorkPlan saved = service.saveWorkPlan(d, null);
        assertNull(saved.getInfra());
        assertNull(saved.getRegionCode());
        assertNull(saved.getRegionCityNm());
        assertNull(saved.getTargetSysNm());
    }

    @Test
    void infra_selected_serverRecalculatesRegion() {
        WorkPlanDTO d = baseDto("INSTALL");
        d.setInfraId(5L);
        // 클라이언트가 엉뚱한 region 을 보내도 무시되어야 함(서버 재계산)
        d.setRegionCityNm("조작된시도");
        d.setTargetSysNm("조작된시스템");
        Infra infra = new Infra();
        infra.setInfraId(5L); infra.setCityNm("경기도"); infra.setDistNm("김포시"); infra.setSysNm("GeoNURIS");
        when(infraRepository.findById(5L)).thenReturn(Optional.of(infra));
        when(sigunguCodeRepository.findBySidoNmAndSggNm("경기도", "김포시"))
                .thenReturn(List.of(sgg("41570", "경기도", "김포시")));

        WorkPlan saved = service.saveWorkPlan(d, null);

        assertNotNull(saved.getInfra());
        assertEquals("경기도", saved.getRegionCityNm());
        assertEquals("김포시", saved.getRegionDistNm());
        assertEquals("GeoNURIS", saved.getTargetSysNm());
        assertEquals("41570", saved.getRegionCode());
    }

    @Test
    void infra_docheong_mapsToSidoSelfRow() {
        // 도청(시군구 아님) → 시도 self-행 매핑(§5-4)
        WorkPlanDTO d = baseDto("INSTALL");
        d.setInfraId(97L);
        Infra infra = new Infra();
        infra.setInfraId(97L); infra.setCityNm("경상남도"); infra.setDistNm("도청"); infra.setSysNm("도시계획정보체계");
        when(infraRepository.findById(97L)).thenReturn(Optional.of(infra));
        when(sigunguCodeRepository.findBySidoNmAndSggNm("경상남도", "경상남도"))
                .thenReturn(List.of(sgg("64800", "경상남도", "경상남도")));

        WorkPlan saved = service.saveWorkPlan(d, null);
        assertEquals("64800", saved.getRegionCode());
    }

    // === 정규화 헬퍼(static) ===

    @Test
    void stripSuffix_removesParenthetical() {
        assertEquals("고성군", WorkPlanService.stripSuffix("고성군(강원도)"));
        assertEquals("당진시", WorkPlanService.stripSuffix("당진시(가상화)"));
        assertEquals("김포시", WorkPlanService.stripSuffix("김포시"));
    }

    @Test
    void sanitizeSysNm_trimsControlHtmlLength() {
        assertEquals("a b", WorkPlanService.sanitizeSysNm("  a   b  "));
        assertNull(WorkPlanService.sanitizeSysNm("   "));
        assertNull(WorkPlanService.sanitizeSysNm(null));
        assertEquals("script", WorkPlanService.sanitizeSysNm("<script>"));
        assertTrue(WorkPlanService.sanitizeSysNm("가".repeat(150)).length() <= 100);
    }
}
