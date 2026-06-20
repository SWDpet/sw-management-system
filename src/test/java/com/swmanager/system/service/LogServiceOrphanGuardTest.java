package com.swmanager.system.service;

import com.swmanager.system.repository.AccessLogRepository;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * LogService.applyOrphanGuard 단위 테스트 (PIT 보강 — access-log-userid-fix 가드 분기 전수 검증).
 *
 * Orphan Guard 규칙:
 *  - null/blank → ANONYMOUS_USERID
 *  - SYSTEM_USERIDS 화이트리스트(anonymousUser/system/scheduler) → 원본 그대로(DB 조회 생략)
 *  - users 에 존재 → 원본 그대로
 *  - users 에 없음(orphan) → WARN + ANONYMOUS_USERID
 */
@ExtendWith(MockitoExtension.class)
class LogServiceOrphanGuardTest {

    @Mock AccessLogRepository accessLogRepository;
    @Mock UserRepository userRepository;

    @InjectMocks LogService logService;

    @Test
    void nullOrBlank_returnsAnonymous() {
        assertThat(logService.applyOrphanGuard(null)).isEqualTo(LogService.ANONYMOUS_USERID);
        assertThat(logService.applyOrphanGuard("   ")).isEqualTo(LogService.ANONYMOUS_USERID);
        verifyNoInteractions(userRepository);
    }

    @Test
    void systemWhitelist_returnsAsIs_withoutDbLookup() {
        assertThat(logService.applyOrphanGuard("system")).isEqualTo("system");
        assertThat(logService.applyOrphanGuard("scheduler")).isEqualTo("scheduler");
        assertThat(logService.applyOrphanGuard(LogService.ANONYMOUS_USERID))
                .isEqualTo(LogService.ANONYMOUS_USERID);
        verifyNoInteractions(userRepository);   // 화이트리스트는 DB 조회 생략(핫패스)
    }

    @Test
    void existingUser_returnsUserid() {
        when(userRepository.existsByUserid("realuser")).thenReturn(true);
        assertThat(logService.applyOrphanGuard("realuser")).isEqualTo("realuser");
    }

    @Test
    void orphanUser_fallsBackToAnonymous() {
        when(userRepository.existsByUserid("ghost")).thenReturn(false);
        assertThat(logService.applyOrphanGuard("ghost")).isEqualTo(LogService.ANONYMOUS_USERID);
    }
}
