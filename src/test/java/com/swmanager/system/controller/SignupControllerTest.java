package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SignupController 단위 테스트 (beyond-A — coverage-misc-controllers).
 * 필드주입(userRepo·BCryptPasswordEncoder·logService) reflection 주입.
 * ⚠컨트롤러가 직접 세팅하는 필드만 검증(@PrePersist 대상 나머지 auth 는 mock-save 라 미실행 → 제외).
 */
class SignupControllerTest {

    private SignupController controller;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new SignupController();
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        logService = mock(LogService.class);
        inject("userRepository", userRepository);
        inject("passwordEncoder", passwordEncoder);
        inject("logService", logService);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = SignupController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    @Test
    void signupForm_returnsView() { // T-S1
        assertThat(controller.signupForm()).isEqualTo("signup");
    }

    @Test
    void register_encodesPwSetsDefaults_savesAndRedirects() { // T-S2
        when(passwordEncoder.encode("raw")).thenReturn("ENC");
        User input = new User();
        input.setUserid("u1");
        input.setUsername("홍길동");
        input.setPassword("raw");

        String view = controller.register(input);
        assertThat(view).isEqualTo("redirect:/login?msg=pending");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        User saved = cap.getValue();
        assertThat(saved.getPassword()).isEqualTo("ENC");   // 인코딩 적용
        assertThat(saved.isEnabled()).isFalse();            // 승인 대기
        assertThat(saved.getUserRole()).isEqualTo("ROLE_USER");
        assertThat(saved.getAuthDashboard()).isEqualTo("NONE");
        assertThat(saved.getAuthProject()).isEqualTo("NONE");
        assertThat(saved.getAuthPerson()).isEqualTo("NONE");
        verify(logService).log(any(String.class),
                any(com.swmanager.system.constant.enums.AccessActionType.class), any(String.class));
    }
}
