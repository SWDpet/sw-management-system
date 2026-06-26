package com.swmanager.system.controller;

import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.User;
import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PersonController(담당자 관리) 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 10탄).
 *
 * <p>PersonController 는 필드 주입(@Autowired 5)이고 권한은 {@link SecurityContextHolder} principal 의
 * authPerson(getAuth)에서 읽으므로, mock 5종을 reflection 으로 필드 주입하고 SecurityContext 를 직접
 * 세팅한 뒤 메서드를 직접 호출한다(DocumentController 패턴). 가드 위반은 {@link InsufficientPermissionException}
 * throw(save 만 403 ResponseEntity). 실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영.
 */
class PersonControllerTest {

    private PersonController controller;
    private PersonInfoRepository personInfoRepository;
    private LogService logService;
    private MessageResolver messages;
    private SigunguCodeRepository sigunguRepository;
    private SysMstRepository sysMstRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new PersonController();
        personInfoRepository = mock(PersonInfoRepository.class);
        logService = mock(LogService.class);
        messages = mock(MessageResolver.class);
        sigunguRepository = mock(SigunguCodeRepository.class);
        sysMstRepository = mock(SysMstRepository.class);
        inject("personInfoRepository", personInfoRepository);
        inject("logService", logService);
        inject("messages", messages);
        inject("sigunguRepository", sigunguRepository);
        inject("sysMstRepository", sysMstRepository);
        when(sigunguRepository.findDistinctSidoNm()).thenReturn(List.of());
        when(sysMstRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = PersonController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authPerson) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole("ROLE_USER"); u.setAuthPerson(authPerson);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginEdit()  { login("EDIT"); }
    private void loginView()  { login("VIEW"); }
    private void loginNone()  { login("NONE"); }

    private static Model model() { return new ExtendedModelMap(); }
    private static Pageable page() { return PageRequest.of(0, 10); }

    // ───────────────────────── 목록 ─────────────────────────

    @Test
    void list_none_throws() {
        loginNone();
        assertThatThrownBy(() -> controller.list(model(), page(), ""))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(personInfoRepository, never()).findAllByKeyword(any(), any());
    }

    @Test
    void list_view_renders() {
        loginView();
        when(personInfoRepository.findAllByKeyword(any(), any())).thenReturn(Page.empty());
        Model m = model();
        assertThat(controller.list(m, page(), "kim")).isEqualTo("person-list");
        assertThat(m.getAttribute("userAuth")).isEqualTo("VIEW");
        assertThat(m.getAttribute("kw")).isEqualTo("kim");
    }

    // ───────────────────────── 상세 ─────────────────────────

    @Test
    void detail_none_throws() {
        loginNone();
        assertThatThrownBy(() -> controller.detail(1L, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void detail_found_renders() {
        loginView();
        PersonInfo p = new PersonInfo();
        p.setUserNm("홍길동");
        when(personInfoRepository.findById(1L)).thenReturn(Optional.of(p));
        Model m = model();
        assertThat(controller.detail(1L, m)).isEqualTo("person-form");
        assertThat(m.getAttribute("person")).isSameAs(p);
        assertThat(m.getAttribute("isDetail")).isEqualTo(true);
    }

    @Test
    void detail_notFound_throws() {
        loginView();
        when(personInfoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.detail(1L, model()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── 등록 폼 ─────────────────────────

    @Test
    void createForm_nonEdit_throws() {
        loginView();
        assertThatThrownBy(() -> controller.createForm(model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void createForm_edit_renders() {
        loginEdit();
        Model m = model();
        assertThat(controller.createForm(m)).isEqualTo("person-form");
        assertThat(m.getAttribute("isDetail")).isEqualTo(false);
        assertThat(m.containsAttribute("sidoList")).isTrue();
    }

    // ───────────────────────── 저장 ─────────────────────────

    @Test
    void save_nonEdit_forbidden() {
        loginView();
        ResponseEntity<String> res = controller.save(new PersonInfo());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(personInfoRepository, never()).save(any());
    }

    @Test
    void save_new_ok() {
        loginEdit();
        when(personInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PersonInfo p = new PersonInfo(); // id null → 신규
        p.setUserNm("새담당자");
        ResponseEntity<String> res = controller.save(p);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(personInfoRepository).save(p);
    }

    @Test
    void save_existing_ok() {
        loginEdit();
        when(personInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PersonInfo p = new PersonInfo();
        p.setId(7L);
        p.setUserNm("기존담당자");
        ResponseEntity<String> res = controller.save(p);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(personInfoRepository).save(p);
    }

    @Test
    void save_serviceThrows_badRequest() {
        loginEdit();
        when(personInfoRepository.save(any())).thenThrow(new RuntimeException("boom"));
        ResponseEntity<String> res = controller.save(new PersonInfo());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ───────────────────────── 삭제 ─────────────────────────

    @Test
    void delete_nonEdit_throws() {
        loginView();
        assertThatThrownBy(() -> controller.delete(1L))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(personInfoRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_edit_deletes() {
        loginEdit();
        PersonInfo p = new PersonInfo();
        p.setUserNm("삭제대상");
        when(personInfoRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThat(controller.delete(1L)).isEqualTo("redirect:/person/list");
        verify(personInfoRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_redirectsWithoutDelete() {
        loginEdit();
        when(personInfoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.delete(1L)).isEqualTo("redirect:/person/list");
        verify(personInfoRepository, never()).deleteById(anyLong());
    }

    // ───────────────────────── API ─────────────────────────

    @Test
    void getSigunguList_returnsRows() {
        com.swmanager.system.domain.SigunguCode sgg = new com.swmanager.system.domain.SigunguCode();
        sgg.setAdmSectC("11110"); sgg.setSidoNm("서울특별시"); sgg.setSggNm("종로구");
        when(sigunguRepository.findBySidoNmOrderBySggNm("서울특별시")).thenReturn(List.of(sgg));
        var rows = controller.getSigunguList("서울특별시");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getSggNm()).isEqualTo("종로구");
    }
}
