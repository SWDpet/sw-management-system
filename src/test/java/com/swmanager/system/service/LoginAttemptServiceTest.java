package com.swmanager.system.service;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.SecurityLoginProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LoginAttemptService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 생성자 주입이라 직접 생성. 계정 잠금/자동해제·남은 시간·실패 카운트 임계 잠금·성공 리셋 커버.
 */
class LoginAttemptServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final SecurityLoginProperties loginProps = mock(SecurityLoginProperties.class);

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(userRepository, loginProps);
        lenient().when(loginProps.getMaxFailedAttempts()).thenReturn(5);
        lenient().when(loginProps.getLockTimeMinutes()).thenReturn(15);
    }

    private User user() {
        User u = new User();
        u.setUserid("tester");
        return u;
    }

    // ===== isLocked =====

    @Test
    void isLocked_noLockTime_false() {
        assertThat(service.isLocked(user())).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void isLocked_active_true() {
        User u = user();
        u.setLockTime(LocalDateTime.now());          // 방금 잠금 → 15분 미경과
        u.setFailedAttempts(5);
        assertThat(service.isLocked(u)).isTrue();
        verify(userRepository, never()).save(any());  // 해제 안 함
    }

    @Test
    void isLocked_expired_autoUnlocksAndSaves() {
        User u = user();
        u.setLockTime(LocalDateTime.now().minusMinutes(20));   // 15분 경과
        u.setFailedAttempts(5);

        assertThat(service.isLocked(u)).isFalse();
        assertThat(u.getFailedAttempts()).isZero();             // 자동 리셋
        assertThat(u.getLockTime()).isNull();
        verify(userRepository).save(u);
    }

    // ===== getRemainingLockMinutes =====

    @Test
    void getRemainingLockMinutes_noLock_zero() {
        assertThat(service.getRemainingLockMinutes(user())).isZero();
    }

    @Test
    void getRemainingLockMinutes_active_aroundLockWindow() {
        User u = user();
        u.setLockTime(LocalDateTime.now());
        // ≈ 15분(+1 올림). 하한을 13 으로 둬 테스트 실행 지연(내부 now() 경과) 흡수.
        assertThat(service.getRemainingLockMinutes(u)).isBetween(13L, 16L);
    }

    @Test
    void getRemainingLockMinutes_expired_zero() {
        User u = user();
        u.setLockTime(LocalDateTime.now().minusMinutes(30));
        assertThat(service.getRemainingLockMinutes(u)).isZero();             // max(0, 음수)
    }

    // ===== loginFailed =====

    @Test
    void loginFailed_belowThreshold_incrementsNoLock() {
        User u = user();
        u.setFailedAttempts(2);
        boolean locked = service.loginFailed(u);
        assertThat(locked).isFalse();
        assertThat(u.getFailedAttempts()).isEqualTo(3);
        assertThat(u.getLockTime()).isNull();
        verify(userRepository).save(u);
    }

    @Test
    void loginFailed_reachesThreshold_locks() {
        User u = user();
        u.setFailedAttempts(4);                 // +1 = 5 == max
        boolean locked = service.loginFailed(u);
        assertThat(locked).isTrue();
        assertThat(u.getFailedAttempts()).isEqualTo(5);
        assertThat(u.getLockTime()).isNotNull();
        verify(userRepository).save(u);
    }

    @Test
    void loginFailed_nullFailedAttempts_startsAtOne() {
        User u = user();                        // failedAttempts null
        boolean locked = service.loginFailed(u);
        assertThat(locked).isFalse();
        assertThat(u.getFailedAttempts()).isEqualTo(1);
        verify(userRepository).save(u);   // null-start 경로도 증가분 영속
    }

    // ===== loginSucceeded =====

    @Test
    void loginSucceeded_resetsWhenFailuresExist() {
        User u = user();
        u.setFailedAttempts(3);
        u.setLockTime(LocalDateTime.now());
        service.loginSucceeded(u);
        assertThat(u.getFailedAttempts()).isZero();
        assertThat(u.getLockTime()).isNull();
        verify(userRepository).save(u);
    }

    @Test
    void loginSucceeded_noResetWhenAlreadyZero() {
        User u = user();
        u.setFailedAttempts(0);
        service.loginSucceeded(u);
        verify(userRepository, never()).save(any());   // 변경 없음 → 저장 안 함
    }

    @Test
    void loginSucceeded_zeroFailuresWithLockTime_noReset() {
        // 복합조건: 리셋은 failedAttempts>0 일 때만 → 0실패+잠금 상태면 손대지 않음(현 동작 문서화)
        User u = user();
        u.setFailedAttempts(0);
        u.setLockTime(LocalDateTime.now());
        service.loginSucceeded(u);
        assertThat(u.getLockTime()).isNotNull();          // 잠금 그대로
        verify(userRepository, never()).save(any());
    }

    // ===== findUser =====

    @Test
    void findUser_delegatesToRepo() {
        User u = user();
        when(userRepository.findByUserid("tester")).thenReturn(Optional.of(u));
        assertThat(service.findUser("tester")).containsSame(u);
    }
}
