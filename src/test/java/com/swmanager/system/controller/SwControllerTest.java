package com.swmanager.system.controller;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.exception.ResourceNotFoundException;
import com.swmanager.system.repository.ContFrmMstRepository;
import com.swmanager.system.repository.ContStatMstRepository;
import com.swmanager.system.repository.MaintTpMstRepository;
import com.swmanager.system.repository.PrjTypesRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.response.ApiResponse;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.SwService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SwController(사업/SW 프로젝트 관리) 단위 테스트 (beyond-A 커버리지 — coverage-sw-controller).
 *
 * <p>기획서 docs/product-specs/coverage-sw-controller.md / 개발계획 docs/exec-plans/coverage-sw-controller.md
 * (codex APPROVE-WITH-FIX 보완반영 + 사용자 최종승인). 필드주입(@Autowired 10) + SecurityContextHolder
 * 권한 → mock reflection 주입 + SecurityContext 직접세팅 후 메서드 직접호출.
 *
 * <p>권한 매트릭스(기획서 §2-1): 웹 편집은 EDIT 전용(ADMIN 우회 없음), REST 는 ROLE_ADMIN 우회.
 * REST 404 는 getProjectApi 한정. 실 Postgres 무접촉.
 */
class SwControllerTest {

    private SwController controller;
    private SwService swService;
    private LogService logService;
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;
    private SigunguCodeRepository sigunguRepository;
    private SysMstRepository sysMstRepository;
    private ContFrmMstRepository contFrmRepository;
    private PrjTypesRepository prjTypesRepository;
    private MaintTpMstRepository maintTpRepository;
    private ContStatMstRepository contStatRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new SwController();
        swService = mock(SwService.class);
        logService = mock(LogService.class);
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        sigunguRepository = mock(SigunguCodeRepository.class);
        sysMstRepository = mock(SysMstRepository.class);
        contFrmRepository = mock(ContFrmMstRepository.class);
        prjTypesRepository = mock(PrjTypesRepository.class);
        maintTpRepository = mock(MaintTpMstRepository.class);
        contStatRepository = mock(ContStatMstRepository.class);
        inject("swService", swService);
        inject("logService", logService);
        inject("swProjectRepository", swProjectRepository);
        inject("userRepository", userRepository);
        inject("sigunguRepository", sigunguRepository);
        inject("sysMstRepository", sysMstRepository);
        inject("contFrmRepository", contFrmRepository);
        inject("prjTypesRepository", prjTypesRepository);
        inject("maintTpRepository", maintTpRepository);
        inject("contStatRepository", contStatRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = SwController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authProject, String role) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUsername("테스터");
        u.setUserRole(role);
        u.setAuthProject(authProject);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginView()  { login("VIEW", "ROLE_USER"); }
    private void loginEdit()  { login("EDIT", "ROLE_USER"); }
    private void loginNone()  { login("NONE", "ROLE_USER"); }
    private void loginAdminNoEdit() { login("NONE", "ROLE_ADMIN"); } // role=ADMIN 이나 authProject=NONE

    private static Model model() { return new ExtendedModelMap(); }

    /** addFormRefData() 가 호출하는 7개 참조 데이터 stub. */
    private void stubFormRef() {
        when(userRepository.findByEnabledTrue()).thenReturn(java.util.List.of());
        when(sigunguRepository.findDistinctSidoNm()).thenReturn(java.util.List.of());
        when(sysMstRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(java.util.List.of());
        when(contFrmRepository.findAll()).thenReturn(java.util.List.of());
        when(prjTypesRepository.findAll()).thenReturn(java.util.List.of());
        when(maintTpRepository.findAll()).thenReturn(java.util.List.of());
        when(contStatRepository.findAll()).thenReturn(java.util.List.of());
    }

    // ───────────────────────── 시군구 API ─────────────────────────

    @Test
    void districts_delegates() { // T-1
        when(sigunguRepository.findBySidoNmOrderBySggNm("서울특별시")).thenReturn(java.util.List.of());
        assertThat(controller.getDistricts("서울특별시")).isNotNull();
    }

    @Test
    void distOptions_blankCity_empty() { // T-2
        assertThat(controller.getDistOptions("  ")).isEmpty();
        verify(swProjectRepository, never()).findAllDistinctDistNmsByCity(any());
    }

    @Test
    void distOptions_withCity_delegates() { // T-3
        when(swProjectRepository.findAllDistinctDistNmsByCity("서울특별시")).thenReturn(java.util.List.of("종로구"));
        assertThat(controller.getDistOptions("서울특별시")).containsExactly("종로구");
    }

    // ───────────────────────── 웹 목록/상세 ─────────────────────────

    @Test
    void list_notLoggedIn_redirectsLogin() { // T-4
        assertThat(controller.projectList(model(), 0, "", null, null, null, null)).isEqualTo("redirect:/login");
        verify(swService, never()).search(any(), any(), any(), any(), any(), any());
    }

    @Test
    void list_none_throws() { // T-5
        loginNone();
        assertThatThrownBy(() -> controller.projectList(model(), 0, "", null, null, null, null))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(swService, never()).search(any(), any(), any(), any(), any(), any());
    }

    @Test
    void list_view_renders_negativePageClamped() { // T-6
        loginView();
        when(swService.search(any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(swProjectRepository.findDistinctYears()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctCityNms()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctSysNmEns()).thenReturn(java.util.List.of());
        Model m = model();
        assertThat(controller.projectList(m, -5, "kw", "2026", null, null, null)).isEqualTo("project-list");
        assertThat(m.containsAttribute("paging")).isTrue();
    }

    @Test
    void list_withCity_loadsDistrictOptions() { // T-7
        loginView();
        when(swService.search(any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(swProjectRepository.findDistinctYears()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctCityNms()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctSysNmEns()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctDistNmsByCity("서울특별시")).thenReturn(java.util.List.of("종로구"));
        Model m = model();
        controller.projectList(m, 0, "", null, "서울특별시", null, null);
        assertThat(m.getAttribute("districtOptions")).isEqualTo(java.util.List.of("종로구"));
    }

    @Test
    void detail_none_throws() { // T-8
        loginNone();
        assertThatThrownBy(() -> controller.detail(1L, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void detail_found_renders() { // T-9
        loginView();
        when(swService.getProject(1L)).thenReturn(new SwProject());
        stubFormRef();
        Model m = model();
        assertThat(controller.detail(1L, m)).isEqualTo("project-detail");
        assertThat(m.containsAttribute("project")).isTrue();
    }

    @Test
    void detail_notFound_throws() { // T-10
        loginView();
        when(swService.getProject(1L)).thenReturn(null);
        assertThatThrownBy(() -> controller.detail(1L, model()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ───────────────────────── 웹 폼/저장/삭제 (EDIT 전용) ─────────────────────────

    @Test
    void newProject_nonEdit_throws() { // T-11
        loginView();
        assertThatThrownBy(() -> controller.newProject(model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void newProject_adminButNotEdit_throws() { // T-12 — 웹 ADMIN 우회 없음
        loginAdminNoEdit();
        assertThatThrownBy(() -> controller.newProject(model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void newProject_edit_renders() { // T-13
        loginEdit();
        stubFormRef();
        Model m = model();
        assertThat(controller.newProject(m)).isEqualTo("project-form");
        assertThat(m.containsAttribute("project")).isTrue();
    }

    @Test
    void form_noId_newDto() { // T-14
        loginEdit();
        stubFormRef();
        assertThat(controller.form(null, model())).isEqualTo("project-form");
    }

    @Test
    void form_withId_loadsProject() { // T-15
        loginEdit();
        when(swService.getProject(1L)).thenReturn(new SwProject());
        stubFormRef();
        Model m = model();
        assertThat(controller.form(1L, m)).isEqualTo("project-form");
        assertThat(m.containsAttribute("project")).isTrue();
    }

    @Test
    void form_notFound_throws() { // T-16
        loginEdit();
        when(swService.getProject(1L)).thenReturn(null);
        assertThatThrownBy(() -> controller.form(1L, model()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void save_nonEdit_throws() { // T-17
        loginView();
        assertThatThrownBy(() -> controller.saveProject(new SwProject()))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(swService, never()).save(any());
    }

    @Test
    void save_new_redirectsStatus() { // T-18
        loginEdit();
        SwProject p = new SwProject(); // projId null → 신규
        when(swService.save(p)).thenReturn(p);
        assertThat(controller.saveProject(p)).isEqualTo("redirect:/projects/status");
        verify(swService).save(p);
    }

    @Test
    void save_update_redirectsStatus() { // T-19
        loginEdit();
        SwProject p = new SwProject();
        p.setProjId(7L);
        when(swService.save(p)).thenReturn(p);
        assertThat(controller.saveProject(p)).isEqualTo("redirect:/projects/status");
        verify(swService).save(p);
    }

    @Test
    void delete_nonEdit_throws() { // T-20
        loginView();
        assertThatThrownBy(() -> controller.deleteProject(1L))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(swService, never()).delete(anyLong());
    }

    @Test
    void delete_edit_redirects() { // T-21
        loginEdit();
        when(swService.getProject(1L)).thenReturn(new SwProject());
        assertThat(controller.deleteProject(1L)).isEqualTo("redirect:/projects/status");
        verify(swService).delete(1L);
    }

    // ───────────────────────── REST API ─────────────────────────

    @Test
    void getApi_notLoggedIn_401() { // T-22
        assertThat(controller.getProjectApi(1L).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getApi_none_403() { // T-23
        loginNone();
        assertThat(controller.getProjectApi(1L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(swService, never()).getProject(anyLong());
    }

    @Test
    void getApi_view_ok() { // T-24
        loginView();
        when(swService.getProject(1L)).thenReturn(new SwProject());
        ResponseEntity<ApiResponse<?>> res = cast(controller.getProjectApi(1L));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getApi_admin_ok_evenNone() { // T-25 — REST ROLE_ADMIN 우회
        loginAdminNoEdit();
        when(swService.getProject(1L)).thenReturn(new SwProject());
        assertThat(controller.getProjectApi(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getApi_notFound_throwsResourceNotFound() { // T-26 — 미존재 시 ResourceNotFoundException throw
        // (직접호출이라 HTTP 404 변환은 미발생 — 예외→404 매핑은 GlobalExceptionHandler 테스트가 검증.
        //  REST 404 는 getProjectApi 한정: create/update/delete 는 404 없음[T-32])
        loginView();
        when(swService.getProject(1L)).thenReturn(null);
        assertThatThrownBy(() -> controller.getProjectApi(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createApi_notLoggedIn_401() { // T-27
        assertThat(controller.createProjectApi(new SwProject()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createApi_nonEdit_403() { // T-28
        loginView();
        assertThat(controller.createProjectApi(new SwProject()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(swService, never()).save(any());
    }

    @Test
    void createApi_edit_ok() { // T-29
        loginEdit();
        when(swService.save(any())).thenReturn(new SwProject());
        assertThat(controller.createProjectApi(new SwProject()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateApi_admin_ok() { // T-30 — REST ROLE_ADMIN 우회 + setProjId
        loginAdminNoEdit();
        when(swService.save(any())).thenReturn(new SwProject());
        SwProject body = new SwProject();
        assertThat(controller.updateProjectApi(9L, body).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.getProjId()).isEqualTo(9L); // path id 본문에 주입
    }

    @Test
    void deleteApi_nonEdit_403() { // T-31
        loginView();
        assertThat(controller.deleteProjectApi(1L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(swService, never()).delete(anyLong());
    }

    @Test
    void deleteApi_edit_ok_targetNull_no404() { // T-32 — 미존재여도 200(404 아님)
        loginEdit();
        when(swService.getProject(1L)).thenReturn(null);
        assertThat(controller.deleteProjectApi(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(swService).delete(1L);
    }

    @Test
    void updateApi_notLoggedIn_401() { // T-37
        assertThat(controller.updateProjectApi(1L, new SwProject()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(swService, never()).save(any());
    }

    @Test
    void updateApi_nonEdit_403() { // T-38
        loginView();
        assertThat(controller.updateProjectApi(1L, new SwProject()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(swService, never()).save(any());
    }

    @Test
    void deleteApi_notLoggedIn_401() { // T-39
        assertThat(controller.deleteProjectApi(1L).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(swService, never()).delete(anyLong());
    }

    @SuppressWarnings("unchecked")
    private static ResponseEntity<ApiResponse<?>> cast(ResponseEntity<?> res) {
        return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) res;
    }

    // ───────────────────────── InitBinder LocalDate ─────────────────────────

    private PropertyEditor dateEditor() {
        WebDataBinder binder = new WebDataBinder(new SwProject(), "project");
        controller.initBinder(binder);
        return binder.findCustomEditor(LocalDate.class, null);
    }

    @Test
    void binder_parsesMultipleFormats() { // T-33
        for (String s : new String[]{"2026-02-13", "2026.02.13", "2026. 2. 13.", "20260213"}) {
            PropertyEditor ed = dateEditor(); // 포맷마다 새 editor — stale 값 마스킹 방지
            ed.setAsText(s);
            assertThat(ed.getValue()).as("format=%s", s).isEqualTo(LocalDate.of(2026, 2, 13));
        }
    }

    @Test
    void binder_blank_null() { // T-34
        PropertyEditor ed = dateEditor();
        ed.setAsText("   ");
        assertThat(ed.getValue()).isNull();
    }

    @Test
    void binder_invalid_throws() { // T-35
        PropertyEditor ed = dateEditor();
        assertThatThrownBy(() -> ed.setAsText("bad-date"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void binder_getAsText_formats() { // T-36
        PropertyEditor ed = dateEditor();
        ed.setValue(LocalDate.of(2026, 2, 13));
        assertThat(ed.getAsText()).isEqualTo("2026-02-13");
    }
}
