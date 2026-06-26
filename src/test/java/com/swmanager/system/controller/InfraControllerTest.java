package com.swmanager.system.controller;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.ContFrmMstRepository;
import com.swmanager.system.repository.ContStatMstRepository;
import com.swmanager.system.repository.MaintTpMstRepository;
import com.swmanager.system.repository.PrjTypesRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.InfraService;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InfraController(인프라/서버 관리) 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 9탄).
 *
 * <p>InfraController 는 필드 주입(@Autowired 8)이고 권한은 {@link SecurityContextHolder} principal 의
 * authInfra(getAuth/isAdmin)에서 읽으므로, mock 8종을 reflection 으로 필드 주입하고 SecurityContext 를
 * 직접 세팅한 뒤 메서드를 직접 호출한다(DocumentController 패턴). 실 Postgres 불필요 → 기본 CI floor 반영.
 */
class InfraControllerTest {

    private InfraController controller;
    private InfraService infraService;
    private SysMstRepository sysMstRepository;
    private SigunguCodeRepository sigunguRepository;
    private ContFrmMstRepository contFrmRepository;
    private PrjTypesRepository prjTypesRepository;
    private MaintTpMstRepository maintTpRepository;
    private ContStatMstRepository contStatRepository;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new InfraController();
        infraService = mock(InfraService.class);
        sysMstRepository = mock(SysMstRepository.class);
        sigunguRepository = mock(SigunguCodeRepository.class);
        contFrmRepository = mock(ContFrmMstRepository.class);
        prjTypesRepository = mock(PrjTypesRepository.class);
        maintTpRepository = mock(MaintTpMstRepository.class);
        contStatRepository = mock(ContStatMstRepository.class);
        logService = mock(LogService.class);
        inject("infraService", infraService);
        inject("sysMstRepository", sysMstRepository);
        inject("sigunguRepository", sigunguRepository);
        inject("contFrmRepository", contFrmRepository);
        inject("prjTypesRepository", prjTypesRepository);
        inject("maintTpRepository", maintTpRepository);
        inject("contStatRepository", contStatRepository);
        inject("logService", logService);
        // addCommonAttributes 공통 stub (등록/수정 폼). plain mock() 라 미사용 stub 도 무해.
        when(sysMstRepository.findAll()).thenReturn(List.of());
        when(sigunguRepository.findDistinctSidoNm()).thenReturn(List.of());
        when(contFrmRepository.findAll()).thenReturn(List.of());
        when(prjTypesRepository.findAll()).thenReturn(List.of());
        when(maintTpRepository.findAll()).thenReturn(List.of());
        when(contStatRepository.findAll()).thenReturn(List.of());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = InfraController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authInfra, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole(role); u.setAuthInfra(authInfra);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginEdit()  { login("EDIT", "ROLE_USER"); }
    private void loginView()  { login("VIEW", "ROLE_USER"); }
    private void loginNone()  { login("NONE", "ROLE_USER"); }

    private static Model model() { return new ExtendedModelMap(); }
    private static RedirectAttributes rttr() { return new RedirectAttributesModelMap(); }
    private static org.springframework.data.domain.Pageable page() { return PageRequest.of(0, 10); }

    // ───────────────────────── 목록 ─────────────────────────

    @Test
    void list_none_redirectsHome() {
        loginNone();
        RedirectAttributesModelMap r = new RedirectAttributesModelMap();
        assertThat(controller.list(model(), page(), "PROD", null, r)).isEqualTo("redirect:/");
        assertThat(r.getFlashAttributes().get("errorMessage")).isEqualTo("접근 권한이 없습니다.");
        verify(infraService, never()).getInfraList(any(), any(), any());
    }

    @Test
    void list_view_renders() {
        loginView();
        when(infraService.getInfraList(any(), any(), any())).thenReturn(Page.empty());
        Model m = model();
        assertThat(controller.list(m, page(), "PROD", null, rttr())).isEqualTo("infra-list");
        assertThat(m.getAttribute("userAuth")).isEqualTo("VIEW");
        assertThat(m.getAttribute("currentType")).isEqualTo("PROD");
    }

    // ───────────────────────── 등록 폼 ─────────────────────────

    @Test
    void createForm_nonEdit_redirectsList() {
        loginView();
        RedirectAttributesModelMap r = new RedirectAttributesModelMap();
        assertThat(controller.createForm(model(), r)).isEqualTo("redirect:/infra/list");
        assertThat(r.getFlashAttributes().get("errorMessage")).isEqualTo("등록 권한이 없습니다.");
    }

    @Test
    void createForm_edit_renders() {
        loginEdit();
        Model m = model();
        assertThat(controller.createForm(m, rttr())).isEqualTo("infra-form");
        assertThat(m.containsAttribute("infra")).isTrue();
        assertThat(m.containsAttribute("sysMstList")).isTrue();
    }

    // ───────────────────────── 상세 ─────────────────────────

    @Test
    void detail_none_redirectsHome() {
        loginNone();
        assertThat(controller.detail(1L, model(), rttr())).isEqualTo("redirect:/");
    }

    @Test
    void detail_view_renders() {
        loginView();
        Infra infra = new Infra();
        infra.setSysNm("UPIS");
        when(infraService.getInfraById(1L)).thenReturn(infra);
        Model m = model();
        assertThat(controller.detail(1L, m, rttr())).isEqualTo("infra-detail");
        assertThat(m.getAttribute("infra")).isSameAs(infra);
    }

    // ───────────────────────── 수정 폼 ─────────────────────────

    @Test
    void editForm_nonEdit_redirectsDetail() {
        loginView();
        assertThat(controller.editForm(1L, model(), rttr())).isEqualTo("redirect:/infra/detail/1");
        verify(infraService, never()).getInfraById(anyLong());
    }

    @Test
    void editForm_edit_renders() {
        loginEdit();
        when(infraService.getInfraById(1L)).thenReturn(new Infra());
        Model m = model();
        assertThat(controller.editForm(1L, m, rttr())).isEqualTo("infra-form");
        assertThat(m.containsAttribute("sidoList")).isTrue();
    }

    // ───────────────────────── 저장 ─────────────────────────

    @Test
    void save_nonEdit_redirectsList() {
        loginView();
        assertThat(controller.save(new Infra(), rttr())).isEqualTo("redirect:/infra/list");
        verify(infraService, never()).saveInfra(any());
    }

    @Test
    void save_new_redirectsWithDefaultType() {
        loginEdit();
        Infra infra = new Infra(); // infraId null → 신규, infraType null → PROD
        assertThat(controller.save(infra, rttr())).isEqualTo("redirect:/infra/list?type=PROD");
        verify(infraService).saveInfra(infra);
    }

    @Test
    void save_existing_redirectsWithType() {
        loginEdit();
        Infra infra = new Infra();
        infra.setInfraId(7L);
        infra.setInfraType("TEST");
        assertThat(controller.save(infra, rttr())).isEqualTo("redirect:/infra/list?type=TEST");
        verify(infraService).saveInfra(infra);
    }

    // ───────────────────────── 삭제 ─────────────────────────

    @Test
    void delete_nonEdit_redirectsList() {
        loginView();
        assertThat(controller.delete(1L, rttr())).isEqualTo("redirect:/infra/list");
        verify(infraService, never()).deleteInfra(anyLong());
    }

    @Test
    void delete_edit_deletesAndRedirects() {
        loginEdit();
        Infra target = new Infra();
        target.setSysNm("KRAS");
        when(infraService.getInfraById(1L)).thenReturn(target);
        assertThat(controller.delete(1L, rttr())).isEqualTo("redirect:/infra/list");
        verify(infraService).deleteInfra(1L);
    }

    // ───────────────────────── API ─────────────────────────

    @Test
    void getDistricts_returnsRows() {
        when(sigunguRepository.findBySidoNm("서울특별시")).thenReturn(List.of());
        assertThat(controller.getDistricts("서울특별시")).isEmpty();
    }
}
