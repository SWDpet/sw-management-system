package com.swmanager.system.service;

import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.domain.Infra;
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

    // ===== beyond-A 뮤테이션 강화: saveWorkPlan 전 필드 캡처 + set-null 분기(prior 값) + 헬퍼 분기 =====

    /** saveWorkPlan 기본정보 setter 전 필드 단언(planType·processStep·title·description·location·statusReason·endDate). */
    @Test
    void saveWorkPlan_capturesAllBasicFields() {
        WorkPlanDTO dto = WorkPlanDTO.builder()
                .planType("SUPPORT").processStep(3).title("제목").description("설명")
                .location("위치").startDate("2026-06-01").endDate("2026-06-30")
                .repeatType("WEEKLY").status("IN_PROGRESS").statusReason("사유").build();

        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getPlanType()).isEqualTo("SUPPORT");           // L143
        assertThat(saved.getProcessStep()).isEqualTo(3);                // L144
        assertThat(saved.getTitle()).isEqualTo("제목");                  // L145
        assertThat(saved.getDescription()).isEqualTo("설명");            // L146
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));  // L147
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 30)); // L148(유효 날짜)
        assertThat(saved.getLocation()).isEqualTo("위치");               // L149
        assertThat(saved.getStatusReason()).isEqualTo("사유");           // L152
    }

    /** 계약대상(projId) → 기존 infra 를 null 로 비움(L171). */
    @Test
    void saveWorkPlan_contractTarget_clearsExistingInfra() {
        WorkPlan existing = new WorkPlan();
        existing.setInfra(new Infra());                                  // prior infra
        when(workPlanRepository.findById(11)).thenReturn(Optional.of(existing));
        SwProject proj = new SwProject();
        proj.setProjId(200L); proj.setCityNm("서울특별시"); proj.setDistNm("강남구");
        when(swProjectRepository.findById(200L)).thenReturn(Optional.of(proj));
        when(sigunguCodeRepository.findBySidoNmAndSggNm(anyString(), anyString())).thenReturn(List.of());

        WorkPlanDTO dto = WorkPlanDTO.builder().planId(11).projId(200L).planType("INSPECT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getInfra()).isNull();                          // L171 setInfra(null)
        assertThat(saved.getProject()).isSameAs(proj);
    }

    /** 미계약 직접입력(SUPPORT) → 기존 project/infra 를 null 로 비움(L198·199). */
    @Test
    void saveWorkPlan_directInput_clearsExistingProjectInfra() {
        WorkPlan existing = new WorkPlan();
        existing.setProject(new SwProject()); existing.setInfra(new Infra());
        when(workPlanRepository.findById(13)).thenReturn(Optional.of(existing));
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("42820"); sgg.setSidoNm("강원도"); sgg.setSggNm("고성군");
        when(sigunguCodeRepository.findById("42820")).thenReturn(Optional.of(sgg));

        WorkPlanDTO dto = WorkPlanDTO.builder().planId(13).planType("SUPPORT").title("t")
                .regionCode("42820").targetSysNm("시스템").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getProject()).isNull();                        // L198
        assertThat(saved.getInfra()).isNull();                          // L199
        assertThat(saved.getRegionCode()).isEqualTo("42820");
    }

    /** 대상 없음 → 기존 project/infra/region 전부 비움(L208~210 + clearRegion L257~260). */
    @Test
    void saveWorkPlan_noTarget_clearsExistingProjectInfraAndRegion() {
        WorkPlan existing = new WorkPlan();
        existing.setProject(new SwProject()); existing.setInfra(new Infra());
        existing.setRegionCode("999"); existing.setRegionCityNm("c");
        existing.setRegionDistNm("d"); existing.setTargetSysNm("s");
        when(workPlanRepository.findById(12)).thenReturn(Optional.of(existing));

        WorkPlanDTO dto = WorkPlanDTO.builder().planId(12).planType("SUPPORT").title("t").build();  // 대상 없음
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getProject()).isNull();                        // L208
        assertThat(saved.getInfra()).isNull();                          // L209
        assertThat(saved.getRegionCode()).isNull();                     // L257 clearRegion
        assertThat(saved.getRegionCityNm()).isNull();                   // L258
        assertThat(saved.getRegionDistNm()).isNull();                   // L259
        assertThat(saved.getTargetSysNm()).isNull();                    // L260
    }

    /** resolveRegionCode: cityNm 공백 → regionCode null(L215 isBlank 분기 + EmptyObjectReturnVals kill). */
    @Test
    void saveWorkPlan_contractTarget_blankCityNm_regionCodeNull() {
        SwProject proj = new SwProject();
        proj.setProjId(201L); proj.setCityNm("  "); proj.setDistNm("강남구");
        when(swProjectRepository.findById(201L)).thenReturn(Optional.of(proj));

        WorkPlanDTO dto = WorkPlanDTO.builder().projId(201L).planType("INSPECT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getRegionCode()).isNull();                     // L215 isBlank(cityNm) → null
    }

    /** resolveRegionCode: distNm 도청 → 시도 self-행 직접 조회(L217 도청/본청 분기). */
    @Test
    void saveWorkPlan_contractTarget_provincialOffice_usesSelfRow() {
        SwProject proj = new SwProject();
        proj.setProjId(202L); proj.setCityNm("강원도"); proj.setDistNm("도청");
        when(swProjectRepository.findById(202L)).thenReturn(Optional.of(proj));
        SigunguCode self = new SigunguCode(); self.setAdmSectC("42000");
        when(sigunguCodeRepository.findBySidoNmAndSggNm("강원도", "강원도")).thenReturn(List.of(self));

        WorkPlanDTO dto = WorkPlanDTO.builder().projId(202L).planType("INSPECT").title("t").build();
        WorkPlan saved = service.saveWorkPlan(dto, new User());

        assertThat(saved.getRegionCode()).isEqualTo("42000");           // 도청 → firstAdmSectC(city,city)
        // 도청 분기는 ("강원도","도청") 조회 안 함 → NegateConditionals(L217) mutant 검출
        verify(sigunguCodeRepository, never()).findBySidoNmAndSggNm("강원도", "도청");
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

    // ===== 캘린더/칸반/알림 조회 위임 (beyond-A) =====

    private static WorkPlan workPlan(int id, String title) {
        WorkPlan p = new WorkPlan();
        p.setPlanId(id);
        p.setTitle(title);
        return p;
    }

    @Test
    void getWorkPlansByDateRange_delegatesAndMaps() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);   // start≠end → 인자순서 오류 관찰
        when(workPlanRepository.findByDateRange(start, end)).thenReturn(List.of(workPlan(1, "캘린더")));

        List<WorkPlanDTO> result = service.getWorkPlansByDateRange(start, end);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlanId()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("캘린더");   // fromEntity 매핑 경로
        verify(workPlanRepository).findByDateRange(eq(start), eq(end));
    }

    @Test
    void getWorkPlansByAssigneeAndDateRange_delegatesAndMaps() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);
        when(workPlanRepository.findByAssigneeAndDateRange(9L, start, end))
                .thenReturn(List.of(workPlan(2, "담당자")));

        List<WorkPlanDTO> result = service.getWorkPlansByAssigneeAndDateRange(9L, start, end);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlanId()).isEqualTo(2);
        assertThat(result.get(0).getTitle()).isEqualTo("담당자");
        verify(workPlanRepository).findByAssigneeAndDateRange(eq(9L), eq(start), eq(end));
    }

    @Test
    void getWorkPlansByProcessStep_delegatesAndMaps() {
        List<String> exclude = List.of("DONE", "CANCELED");
        when(workPlanRepository.findByProcessStepAndStatusNotInOrderByStartDateAsc(3, exclude))
                .thenReturn(List.of(workPlan(3, "칸반")));

        List<WorkPlanDTO> result = service.getWorkPlansByProcessStep(3, exclude);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlanId()).isEqualTo(3);
        assertThat(result.get(0).getTitle()).isEqualTo("칸반");
        // excludeStatuses 내용 전달 검증(eq) — identity 는 계약 아님(복사본 허용)
        verify(workPlanRepository).findByProcessStepAndStatusNotInOrderByStartDateAsc(eq(3), eq(exclude));
    }
}
