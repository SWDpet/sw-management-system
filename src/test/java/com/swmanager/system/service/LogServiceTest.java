package com.swmanager.system.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LogService — Orphan Guard 단위 테스트 (S5 access-log-userid-fix)
 *
 * 검증 매트릭스:
 *  T1 — null userid → anonymousUser
 *  T2 — empty userid → anonymousUser
 *  T3 — anonymousUser → 통과 (DB 조회 X)
 *  T4 — system → 통과 (DB 조회 X) — v2 화이트리스트
 *  T5 — scheduler → 통과 (DB 조회 X) — v2 화이트리스트
 *  T6 — valid userid in users → 그대로 유지
 *  T7 — orphan userid (예: '박욱진') → WARN + anonymousUser
 *  T8 — 화이트리스트 유사값 (sys/SYSTEM) → 정상 Guard 경로 (오인 방지)
 */
@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks LogService logService;

    private ListAppender<ILoggingEvent> appender;
    private Logger logServiceLogger;

    @BeforeEach
    void setUp() {
        logServiceLogger = (Logger) LoggerFactory.getLogger(LogService.class);
        appender = new ListAppender<>();
        appender.start();
        logServiceLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logServiceLogger.detachAppender(appender);
    }

    // ─────────────────────────────────
    // T1, T2 — null/empty fallback
    // ─────────────────────────────────
    @Test
    void nullUserid_returnsAnonymousUser_noUserQuery() {
        String result = logService.applyOrphanGuard(null);
        assertThat(result).isEqualTo(LogService.ANONYMOUS_USERID);
        verify(userRepository, never()).existsByUserid(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void blankUserid_returnsAnonymousUser_noUserQuery() {
        String result = logService.applyOrphanGuard("   ");
        assertThat(result).isEqualTo(LogService.ANONYMOUS_USERID);
        verify(userRepository, never()).existsByUserid(org.mockito.ArgumentMatchers.anyString());
    }

    // ─────────────────────────────────
    // T3, T4, T5 — 화이트리스트 (DB 조회 X)
    // ─────────────────────────────────
    @Test
    void anonymousUser_passes_noUserQuery() {
        String result = logService.applyOrphanGuard("anonymousUser");
        assertThat(result).isEqualTo("anonymousUser");
        verify(userRepository, never()).existsByUserid(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void system_userid_bypasses_orphan_guard_and_no_user_query() {
        // v2 — 시스템/배치 경유 로그
        String result = logService.applyOrphanGuard("system");
        assertThat(result).isEqualTo("system");
        verify(userRepository, never()).existsByUserid(org.mockito.ArgumentMatchers.anyString());
        // WARN 발생 X
        assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    @Test
    void scheduler_userid_bypasses_orphan_guard() {
        String result = logService.applyOrphanGuard("scheduler");
        assertThat(result).isEqualTo("scheduler");
        verify(userRepository, never()).existsByUserid(org.mockito.ArgumentMatchers.anyString());
    }

    // ─────────────────────────────────
    // T6 — 정상 userid
    // ─────────────────────────────────
    @Test
    void validUserid_inUsers_remainsUnchanged() {
        when(userRepository.existsByUserid("admin")).thenReturn(true);

        String result = logService.applyOrphanGuard("admin");
        assertThat(result).isEqualTo("admin");
        // WARN 발생 X
        assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    // ─────────────────────────────────
    // T7 — orphan userid → WARN + fallback
    // ─────────────────────────────────
    @Test
    void orphan_userid_triggers_warn_and_falls_back_to_anonymousUser() {
        when(userRepository.existsByUserid("박욱진")).thenReturn(false);

        String result = logService.applyOrphanGuard("박욱진");
        assertThat(result).isEqualTo(LogService.ANONYMOUS_USERID);

        // WARN 로그 검증
        assertThat(appender.list).anyMatch(e ->
                e.getLevel() == Level.WARN
                && e.getFormattedMessage().contains("ACCESS_LOG_USERID_ORPHAN")
                && e.getFormattedMessage().contains("박욱진"));
    }

    // ─────────────────────────────────
    // T8 — 화이트리스트 유사 케이스 (대소문자/축약형) — v2
    // ─────────────────────────────────
    @Test
    void unknown_userid_triggers_guard_even_if_looks_like_system() {
        // 'sys' 는 화이트리스트가 아니므로 정상 Guard 경로
        when(userRepository.existsByUserid("sys")).thenReturn(false);
        String r1 = logService.applyOrphanGuard("sys");
        assertThat(r1).isEqualTo(LogService.ANONYMOUS_USERID);

        // 'SYSTEM' 도 대소문자 정확 매칭이므로 화이트리스트 X
        when(userRepository.existsByUserid("SYSTEM")).thenReturn(false);
        String r2 = logService.applyOrphanGuard("SYSTEM");
        assertThat(r2).isEqualTo(LogService.ANONYMOUS_USERID);
    }
}
