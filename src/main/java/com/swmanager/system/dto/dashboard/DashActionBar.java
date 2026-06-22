package com.swmanager.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대시보드 액션별 건수 막대(@Getter — Thymeleaf {@code ${a.action}/${a.cnt}/${a.w}} 호환).
 * 기존 컨트롤러-로컬 HashMap 을 대체(§6-4 dashboard-typed-model). action 은 repo Object[] 원형 보존. w=막대너비%.
 */
@Getter
@AllArgsConstructor
public class DashActionBar {
    private final Object action;
    private final long cnt;
    private final int w;
}
