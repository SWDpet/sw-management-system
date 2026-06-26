package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import com.swmanager.system.util.MaskingDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MyPageController 직접호출 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 14탄).
 *
 * <p>기존 {@link MyPageControllerGuardTest} 는 마스킹 가드 로직만 시뮬레이션하고 컨트롤러 메서드를 직접
 * 호출하지 않으므로, 본 테스트가 myPage/updateMyInfo/updatePassword 경로를 직접 커버한다.
 * 필드 주입(@Autowired 5) + SecurityContextHolder principal → mock 5종 reflection 주입 + SecurityContext
 * 직접세팅. 실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영.
 */
class MyPageControllerDirectTest {

    private MyPageController controller;
    private UserRepository userRepository;
    private LogService logService;
    private PasswordEncoder passwordEncoder;
    private com.swmanager.system.i18n.MessageResolver messages;
    private MaskingDetector maskingDetector;

    @BeforeEach
    void setUp() throws Exception {
        controller = new MyPageController();
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        messages = mock(com.swmanager.system.i18n.MessageResolver.class);
        maskingDetector = mock(MaskingDetector.class);
        inject("userRepository", userRepository);
        inject("logService", logService);
        inject("passwordEncoder", passwordEncoder);
        inject("messages", messages);
        inject("maskingDetector", maskingDetector);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = MyPageController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    /** principal 로 로그인(userSeq=1). */
    private void login() {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUserRole("ROLE_USER"); // getAuthorities() 가 role 텍스트를 요구
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    /** findById 가 돌려줄 DB User. */
    private User dbUser(String role) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUserRole(role);
        return u;
    }

    private static Model model() { return new ExtendedModelMap(); }
    private static RedirectAttributes rttr() { return new RedirectAttributesModelMap(); }

    private String update(RedirectAttributes r) {
        // ssn 자리는 명백한 합성 placeholder(실제 주민번호 형태 회피)
        return controller.updateMyInfo("개발부", "1팀", "02-1234-5678", "010-1111-2222",
                "a@b.com", "수석", "서울시", "SSN-PLACEHOLDER", "정처기", "특급", "업무", r);
    }

    // ───────────────────────── 마이페이지 조회 ─────────────────────────

    @Test
    void myPage_notLoggedIn_redirectsLogin() {
        assertThat(controller.myPage(model())).isEqualTo("redirect:/login");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void myPage_loggedIn_renders() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser("ROLE_USER")));
        Model m = model();
        assertThat(controller.myPage(m)).isEqualTo("mypage");
        assertThat(m.containsAttribute("user")).isTrue();
    }

    @Test
    void myPage_userNotFound_throws() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.myPage(model()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── 개인정보 수정 ─────────────────────────

    @Test
    void updateMyInfo_notLoggedIn_redirectsLogin() {
        assertThat(update(rttr())).isEqualTo("redirect:/login");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMyInfo_admin_savesEnabledRedirectsHome() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser("ROLE_ADMIN")));
        // maskingDetector 미스텁 → 전부 false → 정상 저장
        assertThat(update(rttr())).isEqualTo("redirect:/");
        ArgumentCaptorUser cap = save();
        assertThat(cap.user.isEnabled()).isTrue();   // 관리자=즉시 활성
        assertThat(cap.user.getTel()).isEqualTo("02-1234-5678");
    }

    @Test
    void updateMyInfo_normalUser_disablesRedirectsLogout() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser("ROLE_USER")));
        assertThat(update(rttr())).isEqualTo("redirect:/logout");
        assertThat(save().user.isEnabled()).isFalse(); // 일반=재승인 대기(비활성)
    }

    @Test
    void updateMyInfo_maskedTel_blockedAndKept() {
        login();
        User db = dbUser("ROLE_ADMIN");
        db.setTel("기존-전화");
        when(userRepository.findById(1L)).thenReturn(Optional.of(db));
        when(maskingDetector.isMaskedTel(eq("02-1234-5678"), any())).thenReturn(true); // 마스킹 감지
        RedirectAttributes r = rttr();
        assertThat(update(r)).isEqualTo("redirect:/");
        // 마스킹 감지 → tel 미변경(기존 보존) + 경고 flash
        assertThat(save().user.getTel()).isEqualTo("기존-전화");
        assertThat(((RedirectAttributesModelMap) r).getFlashAttributes().get("warningMessage").toString())
                .contains("tel");
    }

    // ───────────────────────── 비밀번호 변경 ─────────────────────────

    @Test
    void updatePassword_notLoggedIn_redirectsLogin() {
        assertThat(controller.updatePassword("a", "b", "b", rttr())).isEqualTo("redirect:/login");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_currentMismatch_redirectsMypage() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser("ROLE_USER")));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(false);
        assertThat(controller.updatePassword("wrong", "new1", "new1", rttr())).isEqualTo("redirect:/mypage");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_confirmMismatch_redirectsMypage() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser("ROLE_USER")));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);
        assertThat(controller.updatePassword("cur", "new1", "new2", rttr())).isEqualTo("redirect:/mypage");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_ok_encodesAndSaves() {
        login();
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser("ROLE_USER")));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);
        when(passwordEncoder.encode("new1")).thenReturn("ENC");
        assertThat(controller.updatePassword("cur", "new1", "new1", rttr())).isEqualTo("redirect:/mypage");
        ArgumentCaptorUser cap = save();
        assertThat(cap.user.getPassword()).isEqualTo("ENC");
    }

    // 저장된 User 캡처 헬퍼
    private ArgumentCaptorUser save() {
        org.mockito.ArgumentCaptor<User> c = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(c.capture());
        ArgumentCaptorUser r = new ArgumentCaptorUser();
        r.user = c.getValue();
        return r;
    }
    private static class ArgumentCaptorUser { User user; }
}
