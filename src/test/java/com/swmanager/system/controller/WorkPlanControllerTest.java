package com.swmanager.system.controller;

import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.dto.WorkPlanDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.WorkPlanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WorkPlanController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 5탄).
 *
 * <p>WorkPlanController 는 필드 주입(@Autowired)이고 권한은 {@link SecurityContextHolder} principal 의
 * authWorkPlan(getAuth/isAdmin)에서 읽으므로, mock 6종을 reflection 으로 필드 주입하고
 * SecurityContext 를 직접 세팅한 뒤 메서드를 직접 호출한다(DocumentController 패턴).
 *
 * <p>실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영. 쓰기 경로는 가드(redirect/403)와 서비스
 * 미호출(부수효과 0)을 함께 단언하고, 통과 경로는 mock 으로 happy-path/검증실패 분기를 커버한다.
 */
class WorkPlanControllerTest {

    private WorkPlanController controller;

    private WorkPlanService workPlanService;
    private InfraRepository infraRepository;
    private UserRepository userRepository;
    private SigunguCodeRepository sigunguCodeRepository;
    private SwProjectRepository swProjectRepository;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new WorkPlanController();
        workPlanService = mock(WorkPlanService.class);
        infraRepository = mock(InfraRepository.class);
        userRepository = mock(UserRepository.class);
        sigunguCodeRepository = mock(SigunguCodeRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        logService = mock(LogService.class);
        inject("workPlanService", workPlanService);
        inject("infraRepository", infraRepository);
        inject("userRepository", userRepository);
        inject("sigunguCodeRepository", sigunguCodeRepository);
        inject("swProjectRepository", swProjectRepository);
        inject("logService", logService);
        // [owner-edit-guard] 기본: 대상 업무계획은 로그인 tester(userSeq=1) 소유(개별 테스트 override 가능)
        org.mockito.Mockito.lenient().when(workPlanService.getWorkPlanById(anyInt()))
                .thenReturn(ownedByTester());
        SecurityContextHolder.clearContext();
    }

    /** [owner-edit-guard] 로그인 tester(userSeq=1) 가 작성자인 업무계획. */
    private static User testerUser() { User u = new User(); u.setUserSeq(1L); u.setUserid("tester"); return u; }
    private static WorkPlan ownedByTester() { WorkPlan w = new WorkPlan(); w.setCreatedBy(testerUser()); return w; }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = WorkPlanController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authWorkPlan, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole(role); u.setAuthWorkPlan(authWorkPlan);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginEdit()  { login("EDIT", "ROLE_USER"); }
    private void loginView()  { login("VIEW", "ROLE_USER"); }
    private void loginNone()  { login("NONE", "ROLE_USER"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN"); }

    private static Model model() { return new ExtendedModelMap(); }
    private static RedirectAttributes rttr() { return new RedirectAttributesModelMap(); }

    // ───────────────────────── 캘린더 / 프로세스 (VIEW 이상) ─────────────────────────

    @Test
    void calendar_none_redirectsHome() {
        loginNone();
        assertThat(controller.calendar(model(), rttr())).isEqualTo("redirect:/");
        verify(infraRepository, never()).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void calendar_view_renders() {
        loginView();
        when(infraRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        assertThat(controller.calendar(m, rttr())).isEqualTo("workplan/workplan-calendar");
        assertThat(m.getAttribute("userAuth")).isEqualTo("VIEW");
    }

    @Test
    void processStatus_none_redirectsHome() {
        loginNone();
        assertThat(controller.processStatus(model(), rttr())).isEqualTo("redirect:/");
    }

    @Test
    void processStatus_admin_rendersWithSteps() {
        loginAdmin();
        when(workPlanService.getWorkPlansByProcessStep(anyInt(), anyList())).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        assertThat(controller.processStatus(m, rttr())).isEqualTo("workplan/process-status");
        assertThat(m.getAttribute("totalCount")).isEqualTo(0);
        // 7단계 모두 조회
        verify(workPlanService, org.mockito.Mockito.times(7)).getWorkPlansByProcessStep(anyInt(), anyList());
    }

    // ───────────────────────── 캘린더 이벤트 API (가드 없음) ─────────────────────────

    @Test
    void events_noAssignee_usesDateRange() {
        when(workPlanService.getWorkPlansByDateRange(any(), any())).thenReturn(List.of());
        ResponseEntity<?> res = controller.getCalendarEvents("2026-06-01", "2026-06-30", null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(workPlanService).getWorkPlansByDateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
    }

    @Test
    void events_withAssignee_usesAssigneeRange() {
        when(workPlanService.getWorkPlansByAssigneeAndDateRange(eq(9L), any(), any())).thenReturn(List.of());
        // ISO date-time 입력도 컨트롤러가 substring(0,10) 으로 날짜만 파싱함을 정확값으로 검증.
        ResponseEntity<?> res = controller.getCalendarEvents("2026-06-01T00:00", "2026-06-30T00:00", 9L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(workPlanService).getWorkPlansByAssigneeAndDateRange(
                eq(9L), eq(LocalDate.of(2026, 6, 1)), eq(LocalDate.of(2026, 6, 30)));
    }

    // ───────────────────────── 등록/수정 폼 (EDIT) ─────────────────────────

    @Test
    void createForm_nonEdit_redirectsCalendar() {
        loginView();
        assertThat(controller.createForm(model(), null, null, rttr()))
                .isEqualTo("redirect:/workplan/calendar");
    }

    @Test
    void createForm_edit_rendersForm() {
        loginEdit();
        stubFormAttributes();
        Model m = model();
        String view = controller.createForm(m, "2026-06-10", 3L, rttr());
        assertThat(view).isEqualTo("workplan/workplan-form");
        assertThat(m.containsAttribute("workPlan")).isTrue();
    }

    @Test
    void editForm_nonEdit_redirectsCalendar() {
        loginNone();
        assertThat(controller.editForm(1, model(), rttr()))
                .isEqualTo("redirect:/workplan/calendar");
    }

    @Test
    void editForm_edit_loadsEntity() {
        loginEdit();
        stubFormAttributes();
        when(workPlanService.getWorkPlanById(1)).thenReturn(ownedByTester());   // 작성자 본인
        Model m = model();
        assertThat(controller.editForm(1, m, rttr())).isEqualTo("workplan/workplan-form");
        assertThat(m.containsAttribute("workPlan")).isTrue();
    }

    private void stubFormAttributes() {
        when(infraRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
    }

    // ───────────────────────── 상세 (VIEW) ─────────────────────────

    @Test
    void getDetail_none_forbidden() {
        loginNone();
        ResponseEntity<?> res = controller.getDetail(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(workPlanService, never()).getWorkPlanDTOById(anyInt());
    }

    @Test
    void getDetail_view_ok() {
        loginView();
        when(workPlanService.getWorkPlanDTOById(1)).thenReturn(new WorkPlanDTO());
        assertThat(controller.getDetail(1).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ───────────────────────── 저장 (EDIT) ─────────────────────────

    @Test
    void save_nonEdit_redirectsCalendar() {
        loginView();
        assertThat(controller.save(new WorkPlanDTO(), rttr())).isEqualTo("redirect:/workplan/calendar");
        verify(workPlanService, never()).saveWorkPlan(any(), any());
    }

    @Test
    void save_new_ok_redirectsCalendar() {
        loginEdit();
        WorkPlan saved = new WorkPlan();
        saved.setPlanId(10);
        saved.setTitle("현장방문");
        when(workPlanService.saveWorkPlan(any(), any())).thenReturn(saved);
        WorkPlanDTO dto = new WorkPlanDTO(); // planId null → 신규
        assertThat(controller.save(dto, rttr())).isEqualTo("redirect:/workplan/calendar");
        verify(workPlanService).saveWorkPlan(any(), any());
    }

    @Test
    void save_validationFails_redirectsForm() {
        loginEdit();
        when(workPlanService.saveWorkPlan(any(), any()))
                .thenThrow(new IllegalArgumentException("대상을 선택하세요"));
        WorkPlanDTO dto = new WorkPlanDTO(); // 신규 → /workplan/new
        assertThat(controller.save(dto, rttr())).isEqualTo("redirect:/workplan/new");
    }

    @Test
    void save_validationFails_existing_redirectsEdit() {
        loginEdit();
        when(workPlanService.saveWorkPlan(any(), any()))
                .thenThrow(new IllegalArgumentException("대상을 선택하세요"));
        WorkPlanDTO dto = new WorkPlanDTO();
        dto.setPlanId(7); // 기존 → /workplan/edit/7
        assertThat(controller.save(dto, rttr())).isEqualTo("redirect:/workplan/edit/7");
    }

    // ───────────────────────── 삭제 (EDIT) ─────────────────────────

    @Test
    void delete_nonEdit_redirectsCalendar() {
        loginView();
        assertThat(controller.delete(1, rttr())).isEqualTo("redirect:/workplan/calendar");
        verify(workPlanService, never()).deleteWorkPlan(anyInt());
    }

    @Test
    void delete_edit_deletesAndRedirects() {
        loginEdit();
        WorkPlan target = ownedByTester();   // 작성자 본인
        target.setTitle("삭제대상");
        when(workPlanService.getWorkPlanById(1)).thenReturn(target);
        assertThat(controller.delete(1, rttr())).isEqualTo("redirect:/workplan/calendar");
        verify(workPlanService).deleteWorkPlan(1);
    }

    // ───────────────────────── 상태 변경 API (EDIT) ─────────────────────────

    @Test
    void updateStatus_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.updateStatus(1, "DONE", null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(workPlanService, never()).updateStatus(anyInt(), any(), any());
    }

    @Test
    void updateStatus_edit_ok() {
        loginEdit();
        WorkPlan updated = new WorkPlan();
        updated.setPlanId(1);
        updated.setStatus("DONE");
        when(workPlanService.updateStatus(1, "DONE", "완료")).thenReturn(updated);
        ResponseEntity<?> res = controller.updateStatus(1, "DONE", "완료");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(workPlanService).updateStatus(1, "DONE", "완료");
    }

    // ───────────────────────── 캐스케이드 API (NONE 차단) ─────────────────────────

    @Test
    void sggBySido_none_forbidden() {
        loginNone();
        ResponseEntity<?> res = controller.getSggBySido("서울특별시");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(sigunguCodeRepository, never()).findBySidoNmOrderBySggNm(any());
    }

    @Test
    void sggBySido_view_ok() {
        loginView();
        when(sigunguCodeRepository.findBySidoNmOrderBySggNm("서울특별시")).thenReturn(List.of());
        assertThat(controller.getSggBySido("서울특별시").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void pjtByRegion_none_forbidden() {
        loginNone();
        ResponseEntity<?> res = controller.getPjtByRegion("11110");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void pjtByRegion_unknownCode_emptyList() {
        loginView();
        when(sigunguCodeRepository.findById("00000")).thenReturn(Optional.empty());
        ResponseEntity<List<com.swmanager.system.dto.workplan.PjtOptionRow>> res =
                controller.getPjtByRegion("00000");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEmpty();
    }

    @Test
    void pjtByRegion_found_returnsProjects() {
        loginView();
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("11110"); sgg.setSidoNm("서울특별시"); sgg.setSggNm("종로구");
        when(sigunguCodeRepository.findById("11110")).thenReturn(Optional.of(sgg));
        when(swProjectRepository.findByCityNmAndDistNmIn(eq("서울특별시"), any())).thenReturn(List.of());
        assertThat(controller.getPjtByRegion("11110").getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
