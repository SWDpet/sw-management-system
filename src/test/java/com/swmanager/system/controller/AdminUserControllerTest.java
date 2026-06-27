package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.dto.AdminSensitiveFieldRow;
import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AdminUserController 단위 테스트 (beyond-A 커버리지 증분 B).
 *
 * <p>의존성 3종(UserRepository·LogService·MessageResolver)은 mock 으로 {@link ReflectionTestUtils}
 * 필드주입한다(컨트롤러가 {@code @Autowired} 필드주입이라 생성자가 없다). 권한은
 * {@link SecurityContextHolder} 에 principal 을 직접 세팅해 가드 분기를 재현한다.
 *
 * <p>DB·Spring 컨텍스트 불필요(순수 단위). production 코드는 건드리지 않으며 현 동작을 박제한다 —
 * 권한 가드 테스트는 가드를 "확인"할 뿐 약화시키지 않는다.
 */
class AdminUserControllerTest {

    private AdminUserController controller;
    private UserRepository userRepository;
    private LogService logService;
    private MessageResolver messages;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController();
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        messages = mock(MessageResolver.class);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "logService", logService);
        ReflectionTestUtils.setField(controller, "messages", messages);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ───────────────────────── 헬퍼 ─────────────────────────

    private static User user(long seq, String userid, String username, String role) {
        User u = new User();
        u.setUserSeq(seq);
        u.setUserid(userid);
        u.setUsername(username);
        u.setUserRole(role);
        return u;
    }

    /** 실제 토큰으로 로그인(principal=CustomUserDetails, isAuthenticated=true). */
    private void loginAs(String role) {
        User u = user(1L, "tester", "테스터", role);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    private void loginAdmin() { loginAs("ROLE_ADMIN"); }

    private static Model model() { return new ExtendedModelMap(); }

    private static Page<User> page(List<User> content, Pageable pageable) {
        return new PageImpl<>(content, pageable, content.size());
    }

    // ───────────────────── A. 권한 가드 4분기 ─────────────────────

    @Test
    void guard_unauthenticated_throws() {
        // context 비움 → auth == null
        assertThatThrownBy(() -> controller.userManagement(0, null, null, null, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void guard_nonCustomUserDetailsPrincipal_throws() {
        // principal 이 CustomUserDetails 가 아님 → getCurrentUser null
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", "N/A", List.of()));
        assertThatThrownBy(() -> controller.userManagement(0, null, null, null, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void guard_nonAdminRole_throws() {
        loginAs("ROLE_USER");
        assertThatThrownBy(() -> controller.userManagement(0, null, null, null, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void guard_getPrincipalThrows_caughtAsNull_throws() {
        // getCurrentUser try/catch 분기: getPrincipal 이 예외 → catch → null → 가드 throw
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenThrow(new RuntimeException("boom"));
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertThatThrownBy(() -> controller.userManagement(0, null, null, null, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    // ─────────────── B. userManagement (검색 switch + expand) ───────────────

    @Test
    void userManagement_noSearch_defaultListing_andPageable() {
        loginAdmin();
        when(userRepository.findByEnabledFalse()).thenReturn(List.of());
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findByEnabledTrue(any(Pageable.class)))
                .thenAnswer(inv -> page(List.of(user(2L, "u2", "사용자2", "ROLE_USER")), inv.getArgument(0)));

        Model m = model();
        String view = controller.userManagement(0, null, null, null, m);

        assertThat(view).isEqualTo("admin-user-list");
        verify(userRepository).findByEnabledTrue(cap.capture());
        Pageable p = cap.getValue();
        assertThat(p.getPageNumber()).isZero();
        assertThat(p.getPageSize()).isEqualTo(10);
        assertThat(p.getSort().getOrderFor("regDt")).isNotNull();
        assertThat(p.getSort().getOrderFor("regDt").getDirection()).isEqualTo(Sort.Direction.DESC);
        // model attr 8종 존재
        assertThat(m.getAttribute("pendingUsers")).isNotNull();
        assertThat(m.getAttribute("activeUsers")).isNotNull();
        assertThat(m.getAttribute("activeUserPage")).isNotNull();
        assertThat(m.getAttribute("currentPage")).isEqualTo(0);
        assertThat(m.getAttribute("totalPages")).isNotNull();
        assertThat(m.getAttribute("totalElements")).isNotNull();
        assertThat(m.getAttribute("expandIds")).isNotNull();
        assertThat(m.getAttribute("expandCsv")).isEqualTo("");
    }

    @Test
    void userManagement_searchByEachType_dispatchesAndLabels() {
        loginAdmin();
        when(userRepository.findByEnabledFalse()).thenReturn(List.of());
        Pageable any = PageRequest.of(0, 10);
        when(userRepository.searchByUserid(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));
        when(userRepository.searchByUsername(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));
        when(userRepository.searchByOrgNm(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));
        when(userRepository.searchByDeptNm(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));
        when(userRepository.searchByTeamNm(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));
        when(userRepository.searchByTel(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));
        when(userRepository.searchByEmail(eq("k"), any(Pageable.class))).thenReturn(page(List.of(), any));

        assertLabel("userid", "아이디");
        assertLabel("username", "성명");
        assertLabel("orgNm", "소속기관");
        assertLabel("deptNm", "부서");
        assertLabel("teamNm", "팀");
        assertLabel("tel", "연락처");
        assertLabel("email", "이메일");

        verify(userRepository).searchByUserid(eq("k"), any(Pageable.class));
        verify(userRepository).searchByUsername(eq("k"), any(Pageable.class));
        verify(userRepository).searchByOrgNm(eq("k"), any(Pageable.class));
        verify(userRepository).searchByDeptNm(eq("k"), any(Pageable.class));
        verify(userRepository).searchByTeamNm(eq("k"), any(Pageable.class));
        verify(userRepository).searchByTel(eq("k"), any(Pageable.class));
        verify(userRepository).searchByEmail(eq("k"), any(Pageable.class));
    }

    /** getSearchTypeName 간접검증: searchInfo = "<라벨> 검색: <keyword>". */
    private void assertLabel(String searchType, String expectedLabel) {
        Model m = model();
        String view = controller.userManagement(0, searchType, "k", null, m);
        assertThat(view).isEqualTo("admin-user-list");
        assertThat((String) m.getAttribute("searchInfo")).startsWith(expectedLabel + " 검색: k");
        assertThat(m.getAttribute("searchType")).isEqualTo(searchType);
        assertThat(m.getAttribute("keyword")).isEqualTo("k");
    }

    @Test
    void userManagement_searchUnknownType_fallsBackToEnabledTrue() {
        loginAdmin();
        when(userRepository.findByEnabledFalse()).thenReturn(List.of());
        when(userRepository.findByEnabledTrue(any(Pageable.class)))
                .thenAnswer(inv -> page(List.of(), inv.getArgument(0)));

        Model m = model();
        String view = controller.userManagement(0, "weird", "k", null, m);

        assertThat(view).isEqualTo("admin-user-list");
        // default case → searchInfo 접두 "전체"
        assertThat((String) m.getAttribute("searchInfo")).startsWith("전체 검색: k");
        verify(userRepository).findByEnabledTrue(any(Pageable.class));
    }

    @Test
    void userManagement_expandParsing() {
        loginAdmin();
        when(userRepository.findByEnabledFalse()).thenReturn(List.of());
        when(userRepository.findByEnabledTrue(any(Pageable.class)))
                .thenAnswer(inv -> page(List.of(), inv.getArgument(0)));

        // (a) 정상 CSV
        Model m1 = model();
        controller.userManagement(0, null, null, "1,2,3", m1);
        @SuppressWarnings("unchecked")
        Set<Long> ids1 = (Set<Long>) m1.getAttribute("expandIds");
        assertThat(ids1).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat((String) m1.getAttribute("expandCsv")).isNotBlank(); // 순서 비계약 → exact 비단언

        // (b) 빈 문자열
        Model m2 = model();
        controller.userManagement(0, null, null, "", m2);
        @SuppressWarnings("unchecked")
        Set<Long> ids2 = (Set<Long>) m2.getAttribute("expandIds");
        assertThat(ids2).isEmpty();
        assertThat(m2.getAttribute("expandCsv")).isEqualTo("");

        // (c) 비숫자 토큰 무시
        Model m3 = model();
        controller.userManagement(0, null, null, "1,x,3", m3);
        @SuppressWarnings("unchecked")
        Set<Long> ids3 = (Set<Long>) m3.getAttribute("expandIds");
        assertThat(ids3).containsExactlyInAnyOrder(1L, 3L);
    }

    // ───────────────────────── C. approve ─────────────────────────

    @Test
    void approve_found_savesAndLogs() {
        loginAdmin();
        User u = user(5L, "newbie", "신입", "ROLE_USER");
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        String view = controller.approveUser(5L, "VIEW", "EDIT", "NONE", "VIEW",
                "NONE", "NONE", "NONE", "NONE", "NONE", "NONE");

        assertThat(view).isEqualTo("redirect:/admin/users");
        assertThat(u.isEnabled()).isTrue();
        assertThat(u.getAuthDashboard()).isEqualTo("VIEW");
        assertThat(u.getAuthProject()).isEqualTo("EDIT");
        verify(userRepository).save(u);
        verify(logService).log(eq(MenuName.USER), eq(AccessActionType.APPROVE), anyString());
    }

    @Test
    void approve_notFound_throws() {
        loginAdmin();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(messages.get(eq("error.user.not_found"), any())).thenReturn("없음");

        assertThatThrownBy(() -> controller.approveUser(99L, "NONE", "NONE", "NONE", "NONE",
                "NONE", "NONE", "NONE", "NONE", "NONE", "NONE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── D. update (redirect 4분기) ─────────────────────────

    @Test
    void update_found_savesLogs_andRedirectVariants() {
        loginAdmin();
        User u = user(7L, "edit", "수정", "ROLE_USER");
        when(userRepository.findById(7L)).thenReturn(Optional.of(u));

        // (page=null, expand=null)
        String r1 = update(u, null, null);
        assertThat(r1).isEqualTo("redirect:/admin/users");
        // (page=2, expand=null)
        String r2 = update(u, 2, null);
        assertThat(r2).isEqualTo("redirect:/admin/users?page=2");
        // (page=null, expand="3,4")
        String r3 = update(u, null, "3,4");
        assertThat(r3).isEqualTo("redirect:/admin/users?expand=3,4");
        // (page=2, expand="3,x,4") → 비숫자 무시
        String r4 = update(u, 2, "3,x,4");
        assertThat(r4).isEqualTo("redirect:/admin/users?page=2&expand=3,4");

        assertThat(u.getEmail()).isEqualTo("a@b.c");
        verify(userRepository, org.mockito.Mockito.atLeastOnce()).save(u);
        verify(logService, org.mockito.Mockito.atLeastOnce())
                .log(eq(MenuName.USER), eq(AccessActionType.UPDATE), anyString());
    }

    private String update(User u, Integer page, String expand) {
        return controller.updateUser(u.getUserSeq(), "부서", "팀", "010", "010-0000-0000",
                "a@b.c", "직책", "주소", "ssn", "자격", "특급", "업무",
                "VIEW", "VIEW", "NONE", "VIEW", "NONE", "NONE", "NONE", "NONE", "NONE", "NONE",
                expand, page);
    }

    @Test
    void update_notFound_throws() {
        loginAdmin();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(messages.get(eq("error.user.not_found"), any())).thenReturn("없음");

        assertThatThrownBy(() -> update(user(404L, "x", "x", "ROLE_USER"), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── E. getSensitiveField ─────────────────────────

    @Test
    void sensitive_eachField_ok_andLogs() {
        loginAdmin();
        User u = user(9L, "sens", "민감", "ROLE_USER");
        u.setSsn("900101-1234567");
        u.setTel("02-111-2222");
        u.setMobile("010-3333-4444");
        u.setEmail("user@corp.com");
        u.setAddress("서울시");
        when(userRepository.findById(9L)).thenReturn(Optional.of(u));

        assertSensitive("ssn", "900101-1234567");
        assertSensitive("tel", "02-111-2222");
        assertSensitive("mobile", "010-3333-4444");
        assertSensitive("email", "user@corp.com");
        assertSensitive("address", "서울시");

        verify(logService, org.mockito.Mockito.atLeast(5))
                .log(eq(MenuName.USER), eq(AccessActionType.SENSITIVE_VIEW), anyString());
    }

    private void assertSensitive(String field, String expected) {
        ResponseEntity<?> res = controller.getSensitiveField(9L, field);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isInstanceOf(AdminSensitiveFieldRow.class);
        AdminSensitiveFieldRow row = (AdminSensitiveFieldRow) res.getBody();
        assertThat(row.field()).isEqualTo(field);
        assertThat(row.value()).isEqualTo(expected);
    }

    @Test
    void sensitive_nullValue_replacedWithEmpty() {
        loginAdmin();
        User u = user(10L, "n", "널", "ROLE_USER"); // ssn 미설정 → null
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));

        ResponseEntity<?> res = controller.getSensitiveField(10L, "ssn");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        AdminSensitiveFieldRow row = (AdminSensitiveFieldRow) res.getBody();
        assertThat(row.value()).isEqualTo(""); // null → "" 보존
    }

    @Test
    void sensitive_userNotFound_returns404() {
        loginAdmin();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.getSensitiveField(404L, "ssn");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sensitive_invalidField_returns400() {
        loginAdmin();
        when(userRepository.findById(11L)).thenReturn(Optional.of(user(11L, "i", "잘못", "ROLE_USER")));

        ResponseEntity<?> res = controller.getSensitiveField(11L, "password");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void sensitive_actorUnknown_branch() {
        // ⚠ branch-coverage 전용 인위 케이스: 정상 인증 흐름이 아니다.
        // checkAdminAuth() 의 getCurrentUser() 는 ADMIN 으로 통과시키고(1차 getPrincipal),
        // actor 산출 시점의 getCurrentUser() 는 null 을 반환(2차 getPrincipal)하게 순차 구성하여
        // actor="unknown" fallback 분기에 도달한다.
        User admin = user(1L, "admin", "관리자", "ROLE_ADMIN");
        CustomUserDetails adminCud = new CustomUserDetails(admin);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(adminCud, (Object) null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        User u = user(12L, "t", "대상", "ROLE_USER");
        u.setEmail("x@y.z");
        when(userRepository.findById(12L)).thenReturn(Optional.of(u));

        ResponseEntity<?> res = controller.getSensitiveField(12L, "email");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(logService).log(eq(MenuName.USER), eq(AccessActionType.SENSITIVE_VIEW),
                org.mockito.ArgumentMatchers.contains("unknown"));
    }

    // ───────────────────────── F. delete ─────────────────────────

    @Test
    void delete_found_deletesAndLogs() {
        loginAdmin();
        when(userRepository.findById(13L)).thenReturn(Optional.of(user(13L, "del", "삭제", "ROLE_USER")));

        String view = controller.deleteUser(13L);

        assertThat(view).isEqualTo("redirect:/admin/users");
        verify(userRepository).deleteById(13L);
        verify(logService).log(eq(MenuName.USER), eq(AccessActionType.DELETE),
                org.mockito.ArgumentMatchers.contains("del"));
    }

    @Test
    void delete_notFound_unknownInfo_stillDeletes() {
        loginAdmin();
        when(userRepository.findById(14L)).thenReturn(Optional.empty());

        String view = controller.deleteUser(14L);

        assertThat(view).isEqualTo("redirect:/admin/users");
        verify(userRepository).deleteById(14L);
        verify(logService).log(eq(MenuName.USER), eq(AccessActionType.DELETE),
                org.mockito.ArgumentMatchers.contains("Unknown"));
    }
}
