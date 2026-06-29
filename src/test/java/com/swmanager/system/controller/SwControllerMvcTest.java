package com.swmanager.system.controller;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.exception.GlobalExceptionHandler;
import com.swmanager.system.repository.*;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.SwService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [beyond-A] SwController HTTP 표면 net — standalone MockMvc.
 * 기존 {@code SwControllerTest}(직접 호출)가 못 잡는 <b>HTTP 배선</b>을 박제:
 * 실제 경로 매핑(@GetMapping/@PostMapping/@PathVariable)·HTTP status·redirectedUrl·
 * view name·@RequestParam 바인딩·@ResponseBody JSON content-type·예외→status(GlobalExceptionHandler).
 *
 * <p>DB/Spring 컨텍스트 불요(standaloneSetup) → CI gates(verify)에서 실행, RUN_DB_TESTS 무관.
 * 인증은 getCurrentUser 가 SecurityContextHolder 를 직접 읽으므로 컨텍스트 세팅으로 제어.
 * 컨트롤러는 PIT 부적합(codex 합의) — 본 net 은 회귀방어용(PIT 미편입).
 */
class SwControllerMvcTest {

    private MockMvc mockMvc;
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

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = SwController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authProject) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole("ROLE_USER"); u.setAuthProject(authProject);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    /** /status 가 사용하는 옵션 조회 stub. */
    private void stubStatusOptions() {
        when(swService.search(any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(swProjectRepository.findDistinctYears()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctCityNms()).thenReturn(java.util.List.of());
        when(swProjectRepository.findAllDistinctSysNmEns()).thenReturn(java.util.List.of());
    }

    /** detail/new/form 의 7개 참조 데이터 stub. */
    private void stubFormRef() {
        when(userRepository.findByEnabledTrue()).thenReturn(java.util.List.of());
        when(sigunguRepository.findDistinctSidoNm()).thenReturn(java.util.List.of());
        when(sysMstRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(java.util.List.of());
        when(contFrmRepository.findAll()).thenReturn(java.util.List.of());
        when(prjTypesRepository.findAll()).thenReturn(java.util.List.of());
        when(maintTpRepository.findAll()).thenReturn(java.util.List.of());
        when(contStatRepository.findAll()).thenReturn(java.util.List.of());
    }

    // ── 1. 미인증 → 라우팅 + 3xx redirect /login ─────────────────────────────
    @Test
    void status_notAuthenticated_redirectsLogin() throws Exception {
        mockMvc.perform(get("/projects/status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ── 2. 인증(VIEW) → 200 + view project-list + model paging + 기본 param 바인딩 ──
    @Test
    void status_view_rendersListWithModel() throws Exception {
        login("VIEW");
        stubStatusOptions();
        mockMvc.perform(get("/projects/status"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("kw", ""));   // @RequestParam defaultValue 바인딩
    }

    // ── 3. @RequestParam 바인딩 — page 음수도 정상 바인딩되어 200 ────────────────
    @Test
    void status_negativePageParam_bindsAndRenders() throws Exception {
        login("VIEW");
        stubStatusOptions();
        mockMvc.perform(get("/projects/status").param("page", "-5").param("kw", "x"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attribute("kw", "x"));
    }

    // ── 4. /detail/{id} — @PathVariable 라우팅 + view project-detail ───────────
    @Test
    void detail_view_rendersDetail() throws Exception {
        login("VIEW");
        SwProject p = new SwProject();
        p.setProjId(1L);
        when(swService.getProject(1L)).thenReturn(p);
        stubFormRef();
        mockMvc.perform(get("/projects/detail/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-detail"))
                .andExpect(model().attributeExists("project"));
    }

    // ── 5. /new — EDIT 권한 → view project-form ──────────────────────────────
    @Test
    void newProject_edit_rendersForm() throws Exception {
        login("EDIT");
        stubFormRef();
        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-form"))
                .andExpect(model().attributeExists("project"));
    }

    // ── 6. POST /save — EDIT → 3xx redirect /projects/status ──────────────────
    @Test
    void save_edit_redirectsToStatus() throws Exception {
        login("EDIT");
        mockMvc.perform(post("/projects/save").param("title", "신규사업"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/status"));
        verify(swService).save(any(SwProject.class));
    }

    // ── 7. /api/districts — @ResponseBody JSON content-type ───────────────────
    @Test
    void districts_api_returnsJson() throws Exception {
        when(sigunguRepository.findBySidoNmOrderBySggNm("서울특별시")).thenReturn(java.util.List.of());
        mockMvc.perform(get("/projects/api/districts").param("sido", "서울특별시"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    // ── 8. NONE 권한 → InsufficientPermissionException → GlobalExceptionHandler 403 ──
    @Test
    void status_nonePermission_403ViaHandler() throws Exception {
        login("NONE");
        mockMvc.perform(get("/projects/status"))
                .andExpect(status().isForbidden());   // INSUFFICIENT_PERMISSION(403)
        verify(swService, never()).search(any(), any(), any(), any(), any(), any());
    }
}
