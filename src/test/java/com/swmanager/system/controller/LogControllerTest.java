package com.swmanager.system.controller;

import com.swmanager.system.domain.AccessLog;
import com.swmanager.system.repository.AccessLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LogController — 탭 선택 + 날짜 경계(LocalDateTime) 변환 + 키워드 정규화 검증.
 * (네이티브 쿼리/ADMIN 가드 통합검증은 실 DB·시큐리티 컨텍스트 필요 → 별도 통합테스트.)
 */
class LogControllerTest {

    private LogController controller(AccessLogRepository repo) {
        LogController c = new LogController();
        ReflectionTestUtils.setField(c, "accessLogRepository", repo);
        return c;
    }

    @Test
    void accessTab_callsFindAccessTab_withDateBoundaries() {
        AccessLogRepository repo = mock(AccessLogRepository.class);
        when(repo.findAccessTab(any(), any(), any(), any())).thenReturn(Page.empty());
        Model model = new ExtendedModelMap();

        controller(repo).viewLogs(model, "access", 0, "kim",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10));

        verify(repo).findAccessTab(any(Pageable.class), eq("kim"),
                eq(LocalDate.of(2026, 6, 1).atStartOfDay()),     // fromStart = 시작일 00:00
                eq(LocalDate.of(2026, 6, 11).atStartOfDay()));   // toExclusive = 종료일+1 00:00
        verify(repo, never()).findMenuTab(any(), any(), any(), any());
    }

    @Test
    void menuTab_default_blankKeyword_toNull_noDates() {
        AccessLogRepository repo = mock(AccessLogRepository.class);
        when(repo.findMenuTab(any(), any(), any(), any())).thenReturn(Page.empty());
        Model model = new ExtendedModelMap();

        controller(repo).viewLogs(model, "menu", 0, "   ", null, null);

        // kw 빈값 → null, 날짜 미지정 → 넓은 경계(항상 바인딩) 주입
        verify(repo).findMenuTab(any(Pageable.class), isNull(),
                eq(java.time.LocalDateTime.of(1970, 1, 1, 0, 0)),
                eq(java.time.LocalDateTime.of(9999, 1, 1, 0, 0)));
        verify(repo, never()).findAccessTab(any(), any(), any(), any());
    }
}
