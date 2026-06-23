package com.swmanager.system.service;

import com.swmanager.system.security.SecurityLoginProperties;
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LoginAttemptService 가 SecurityLoginProperties 를 주입받아 임계값대로 동작하는지 검증.
 * 개발계획서 v2 §1 Step 5 / FR-B4.
 *
 * maxFailedAttempts=3 주입 → 3회 실패에서 잠금 확인 (Mockito, Spring 컨텍스트 없음).
 */
class LoginAttemptServiceWithPropertiesTest {

    private UserRepository userRepository;
    private SecurityLoginProperties props;
    private LoginAttemptService svc;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        props = new SecurityLoginProperties();
        props.setMaxFailedAttempts(3);   // 기본 5에서 3으로 변경
        props.setLockTimeMinutes(7);

        svc = new LoginAttemptService(userRepository, props);
    }

    @Test
    void loginFailed_customThreshold_locksAtConfiguredCount() {
        User user = new User();
        user.setUserid("alice");
        user.setFailedAttempts(0);

        boolean lock1 = svc.loginFailed(user);  // 1회
        assertThat(lock1).isFalse();
        assertThat(user.getFailedAttempts()).isEqualTo(1);

        boolean lock2 = svc.loginFailed(user);  // 2회
        assertThat(lock2).isFalse();

        boolean lock3 = svc.loginFailed(user);  // 3회 — 잠금
        assertThat(lock3).isTrue();
        assertThat(user.getLockTime()).isNotNull();
    }

    @Test
    void loginSucceeded_resetsFailCountAndClearsLock() {
        User user = new User();
        user.setUserid("bob");
        user.setFailedAttempts(2);
        user.setLockTime(java.time.LocalDateTime.now());  // 잠김 상태 가정

        svc.loginSucceeded(user);

        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLockTime()).isNull();   // 잠금 해제(setLockTime(null) 호출 검증)
    }

    /** 실패 이력 없음(0) → no-op(저장 안 함). loginSucceeded 의 >0 경계 검증. */
    @Test
    void loginSucceeded_noPriorFailures_doesNotSave() {
        User user = new User();
        user.setUserid("carol");
        user.setFailedAttempts(0);

        svc.loginSucceeded(user);

        verify(userRepository, never()).save(any(User.class));
    }
}
