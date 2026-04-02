package com.swmanager.system.service;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 로그인 시도 제한 서비스
 * - 5회 연속 실패 시 계정 15분 잠금
 * - 잠금 해제 후 자동 카운트 리셋
 *
 * 최적화: User 객체를 직접 받아 DB 중복 조회 제거
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_MINUTES = 15;

    private final UserRepository userRepository;

    // ====== User 객체 기반 메서드 (DB 조회 없음) ======

    /**
     * 계정 잠금 여부 확인 (User 객체 직접 사용, DB 조회 없음)
     * - 잠금 시간 경과 시 자동 해제 후 save
     */
    @Transactional
    public boolean isLocked(User user) {
        if (user.getLockTime() == null) return false;

        LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_MINUTES);
        if (LocalDateTime.now().isAfter(unlockTime)) {
            // 잠금 시간 경과 → 자동 해제
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            log.info("계정 잠금 해제 (시간 경과) - userid: {}", user.getUserid());
            return false;
        }
        return true;
    }

    /**
     * 남은 잠금 시간 (분) — User 객체 직접 사용, DB 조회 없음
     */
    public long getRemainingLockMinutes(User user) {
        if (user.getLockTime() == null) return 0;

        LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_MINUTES);
        long remaining = Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
        return Math.max(0, remaining + 1);
    }

    /**
     * 로그인 실패 처리 (User 객체 직접 사용)
     * @return 잠금 발생 여부
     */
    @Transactional
    public boolean loginFailed(User user) {
        int newFailCount = (user.getFailedAttempts() != null ? user.getFailedAttempts() : 0) + 1;
        user.setFailedAttempts(newFailCount);

        boolean locked = false;
        if (newFailCount >= MAX_FAILED_ATTEMPTS) {
            user.setLockTime(LocalDateTime.now());
            locked = true;
            log.warn("계정 잠금 - userid: {}, 실패횟수: {}", user.getUserid(), newFailCount);
        }

        userRepository.save(user);
        return locked;
    }

    /**
     * 로그인 성공 — 실패 카운트 리셋 (User 객체 직접 사용)
     */
    @Transactional
    public void loginSucceeded(User user) {
        if (user.getFailedAttempts() != null && user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        }
    }

    // ====== userid 기반 메서드 (하위 호환용, 필요 시 사용) ======

    /**
     * userid로 User 조회 (1회 조회 후 User 객체 기반 메서드 사용 권장)
     */
    public Optional<User> findUser(String userid) {
        return userRepository.findByUserid(userid);
    }
}
