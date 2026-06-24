package com.swmanager.system.service;

import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.dto.WorkPlanDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkPlanService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 필드 주입이라 ReflectionTestUtils 로 mock 주입.
 *
 * <p>범위 분담: 캐스케이드 해피패스(계약/SUPPORT/대상없음)·헬퍼 기본은 {@code WorkPlanTargetTest}
 * 가 이미 커버. 본 클래스는 <b>중복을 피하고</b> 나머지 서비스 표면을 커버한다 —
 * CRUD·검색 정규화·담당자/부모/날짜 매핑·상태변경·DTO 매핑, 그리고 target 미커버
 * 캐스케이드 엣지(군위군 행정개편·미매칭 fallback·사업 미존재 clear).
 */
class WorkPlanServiceTest {

    private final WorkPlanRepository workPlanRepository = mock(WorkPlanRepository.class);
    private final InfraRepository infraRepository = mock(InfraRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SigunguCodeRepository sigunguCodeRepository = mock(SigunguCodeRepository.class);
    private final SwProjectRepository swProjectRepository = mock(SwProjectRepository.class);

    private WorkPlanService service;

    @BeforeEach
    void setUp() {
        service = new WorkPlanService();
        ReflectionTestUtils.setField(service, "workPlanRepository", workPlanRepository);
        ReflectionTestUtils.setField(service, "infraRepository", infraRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "sigunguCodeRepository", sigunguCodeRepository);
        ReflectionTestUtils.setField(service, "swProjectRepository", swProjectRepository);
        when(workPlanRepository.save(any(WorkPlan.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ===== 정적 정제 로직(target 미커버 보강): null·정확한 길이 절단 =====

    @Test
    void stripSuffix_null_returnsNull() {
        assertThat(WorkPlanService.stripSuffix(null)).isNull();
    }

    @Test
    void sanitizeSysNm_truncatesToExactly100Chars() {
        assertThat(WorkPlanService.sanitizeSysNm("가".repeat(150))).hasSize(100);
    }

    @Test
    void sanitizeSysNm_compressesWhitespace_andStripsAllHtmlSpecials() {
        String out = WorkPlanService.sanitizeSysNm("  상수도 <관리>  \"시스템\"&'A'  ");
        assertThat(out).isEqualTo("상수도 관리 시스템A")
                .doesNotContain("<", ">", "\"", "'", "&");
    }

    // ===== getWorkPlanById / getWorkPlanDTOById =====

    @Test
    void getWorkPlanById_found() {
        WorkPlan p = new WorkPlan();
        when(workPlanRepository.findById(1)).thenReturn(Optional.of(p));
        assertThat(service.getWorkPlanById(1)).isSameAs(p);
    }

    @Test
    void getWorkPlanById_notFound_throwsWithId() {
        when(workPlanRepository.findById(9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getWorkPlanById(9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9");
    }

    @Test
    void getWorkPlanDTOById_delegatesAndMaps() {
        WorkPlan p = new WorkPlan();
        p.setPlanId(7);
        p.setTitle("점검");
        p.setPlanType("INSPECT");
        when(workPlanRepository.findById(7)).thenReturn(Optional.of(p));
        WorkPlanDTO dto = service.getWorkPlanDTOById(7);
        assertThat(dto.getPlanId()).isEqualTo(7);
        assertThat(dto.getTitle()).isEqualTo("점검");
        assertThat(dto.getPlanType()).isEqualTo("INSPECT");   // 매핑 경로 단언
    }

    // ===== searchWorkPlans: blank → null 정규화 + 위임 =====

    @Test
    void searchWorkPlans_blankTypeAndStatus_normalizedToNull() {
        org.springframework.data.domain.Page<WorkPlan> empty =
                new org.springframework.data.domain.PageImpl<>(List.of());
        when(workPlanRepository.searchWorkPlans(isNull(), isNull(), any(), any(), any()))
                .thenReturn(empty);

        var page = service.searchWorkPlans("  ", "  ", 1L, 2L,
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(page).isEmpty();
        verify(workPlanRepository).searchWorkPlans(isNull(), isNull(), eq(1L), eq(2L), any());
    }

    // ===== saveWorkPlan: 신규 vs 기존 + 기본값 =====

    @Test
    void saveWorkPlan_newPlan_setsCreatedBy_andDefaults() {
        User author = new User();
        WorkPlanDTO dto = WorkPlanDTO.builder()
                .planType("SUPPORT").title("t").build();   // 대상 없음(③)

        WorkPlan saved = service.saveWorkPlan(dto, author);

        assertThat(saved.getCreatedBy()).isSameAs(author);
        assertThat(saved.getRepeatType()).isEqualTo("NONE");          // 기본값
        assertThat(saved.getStatus()).isEqualTo(WorkPlanStatus.PLANNED.name());
    }

    @Test
    void saveWorkPlan_existing_loadsAndDoesNotResetCreatedBy() {
        WorkPlan existing = new WorkPlan();
        User original = new User();
        existing.setCreatedBy(original);
        when(workPlanRepository.findById(5)).thenReturn(Optional.of(existing));

        WorkPlanDTO dto = WorkPlanDTO.builder()
                .planId(5).planType("SUPPORT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved).isSameAs(existing);
        assertThat(saved.getCreatedBy()).isSameAs(original);          // 신규설정 안 함
    }

    @Test
    void saveWorkPlan_existing_notFound_throws() {
        when(workPlanRepository.findById(5)).thenReturn(Optional.empty());
        WorkPlanDTO dto = WorkPlanDTO.builder().planId(5).planType("SUPPORT").title("t").build();
        assertThatThrownBy(() -> service.saveWorkPlan(dto, new User()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== 캐스케이드 엣지(target 미커버): 접미사 strip·군위군·fallback·proj 미존재 =====

    @Test
    void saveWorkPlan_contractTarget_stripsDistSuffixForLookup() {
        SwProject proj = new SwProject();
        proj.setProjId(100L);
        proj.setCityNm("강원도");
        proj.setDistNm("고성군(강원도)");                  // resolveRegionCode 가 접미사 제거 후 조회
        proj.setSysNm("상수도시스템");
        when(swProjectRepository.findById(100L)).thenReturn(Optional.of(proj));
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("42820");
        when(sigunguCodeRepository.findBySidoNmAndSggNm("강원도", "고성군")).thenReturn(List.of(sgg));

        WorkPlanDTO dto = WorkPlanDTO.builder()
                .projId(100L).planType("INSPECT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getRegionCode()).isEqualTo("42820");
        assertThat(saved.getRegionDistNm()).isEqualTo("고성군(강원도)");  // 원본 보존(표시용)
        verify(sigunguCodeRepository).findBySidoNmAndSggNm("강원도", "고성군");
    }

    @Test
    void saveWorkPlan_contractTarget_gunwiCounty_remappedToDaegu() {
        SwProject proj = new SwProject();
        proj.setProjId(102L);
        proj.setCityNm("경상북도");
        proj.setDistNm("군위군");                      // 2023 경북→대구 편입
        when(swProjectRepository.findById(102L)).thenReturn(Optional.of(proj));
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("27720");
        when(sigunguCodeRepository.findBySidoNmAndSggNm("대구광역시", "군위군")).thenReturn(List.of(sgg));

        WorkPlanDTO dto = WorkPlanDTO.builder().projId(102L).planType("INSPECT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getRegionCode()).isEqualTo("27720");
        verify(sigunguCodeRepository).findBySidoNmAndSggNm("대구광역시", "군위군");
    }

    @Test
    void saveWorkPlan_contractTarget_unmatched_fallsBackToSelfRow() {
        SwProject proj = new SwProject();
        proj.setProjId(103L);
        proj.setCityNm("서울특별시");
        proj.setDistNm("없는구");
        when(swProjectRepository.findById(103L)).thenReturn(Optional.of(proj));
        when(sigunguCodeRepository.findBySidoNmAndSggNm("서울특별시", "없는구")).thenReturn(List.of());
        SigunguCode self = new SigunguCode();
        self.setAdmSectC("11000");
        when(sigunguCodeRepository.findBySidoNmAndSggNm("서울특별시", "서울특별시")).thenReturn(List.of(self));

        WorkPlanDTO dto = WorkPlanDTO.builder().projId(103L).planType("INSPECT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getRegionCode()).isEqualTo("11000");          // fallback self-행
    }

    @Test
    void saveWorkPlan_contractTarget_projectNotFound_clearsRegion() {
        when(swProjectRepository.findById(104L)).thenReturn(Optional.empty());
        WorkPlanDTO dto = WorkPlanDTO.builder()
                .projId(104L).planType("INSPECT").title("t").regionCityNm("x").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getProject()).isNull();
        assertThat(saved.getRegionCode()).isNull();
        assertThat(saved.getRegionCityNm()).isNull();
        assertThat(saved.getTargetSysNm()).isNull();
    }

    @Test
    void saveWorkPlan_directInput_sanitizesSysNm_inContext() {
        // SUPPORT 직접입력 경로에서 시스템명 정제가 실제로 적용되는지(헬퍼 단독이 아닌 흐름)
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("42820"); sgg.setSidoNm("강원도"); sgg.setSggNm("고성군");
        when(sigunguCodeRepository.findById("42820")).thenReturn(Optional.of(sgg));
        WorkPlanDTO dto = WorkPlanDTO.builder()
                .planType("SUPPORT").title("t").regionCode("42820")
                .targetSysNm(" 상수도 <시스템> ").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getTargetSysNm()).isEqualTo("상수도 시스템");
        assertThat(saved.getRegionDistNm()).isEqualTo("고성군");        // 서버 출처
    }

    // ===== 담당자/부모계획 연동 + parseDate + 상태/반복 override =====

    @Test
    void saveWorkPlan_setsAssigneeAndParent_andParsesDates() {
        User assignee = new User();
        when(userRepository.findById(3L)).thenReturn(Optional.of(assignee));
        WorkPlan parent = new WorkPlan();
        when(workPlanRepository.findById(8)).thenReturn(Optional.of(parent));

        WorkPlanDTO dto = WorkPlanDTO.builder()
                .planType("SUPPORT").title("t")
                .assigneeId(3L).parentPlanId(8)
                .startDate("2026-06-01").endDate("bad-date")
                .repeatType("MONTHLY").status("IN_PROGRESS").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getAssignee()).isSameAs(assignee);
        assertThat(saved.getParentPlan()).isSameAs(parent);
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(saved.getEndDate()).isNull();                       // 파싱 실패 → null
        assertThat(saved.getRepeatType()).isEqualTo("MONTHLY");
        assertThat(saved.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void saveWorkPlan_nullAssigneeAndParent_clearsBoth() {
        WorkPlan existing = new WorkPlan();
        existing.setAssignee(new User());
        existing.setParentPlan(new WorkPlan());
        when(workPlanRepository.findById(9)).thenReturn(Optional.of(existing));

        WorkPlanDTO dto = WorkPlanDTO.builder()
                .planId(9).planType("SUPPORT").title("t").build();   // assignee/parent null
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getAssignee()).isNull();
        assertThat(saved.getParentPlan()).isNull();
    }

    // ===== updateStatus / deleteWorkPlan =====

    @Test
    void updateStatus_setsStatusAndReason_andSaves() {
        WorkPlan p = new WorkPlan();
        when(workPlanRepository.findById(4)).thenReturn(Optional.of(p));
        WorkPlan out = service.updateStatus(4, "COMPLETED", "완료함");
        assertThat(out.getStatus()).isEqualTo("COMPLETED");
        assertThat(out.getStatusReason()).isEqualTo("완료함");
        verify(workPlanRepository).save(p);
    }

    @Test
    void deleteWorkPlan_delegatesToRepo() {
        service.deleteWorkPlan(6);
        verify(workPlanRepository).deleteById(6);
    }

    // ===== 조회 위임(DTO 매핑) =====

    @Test
    void getWorkPlansByInfra_mapsToDto() {
        WorkPlan p = new WorkPlan();
        p.setPlanId(1); p.setPlanType("INSPECT");
        when(workPlanRepository.findByInfra_InfraIdOrderByStartDateDesc(10L)).thenReturn(List.of(p));
        List<WorkPlanDTO> out = service.getWorkPlansByInfra(10L);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getPlanId()).isEqualTo(1);
        assertThat(out.get(0).getPlanType()).isEqualTo("INSPECT");   // 매핑 경로 단언
    }

    @Test
    void getChildPlans_mapsToDto() {
        WorkPlan child = new WorkPlan();
        child.setPlanId(2); child.setPlanType("SUPPORT");
        when(workPlanRepository.findByParentPlan_PlanId(1)).thenReturn(List.of(child));
        assertThat(service.getChildPlans(1)).hasSize(1);
    }
}
