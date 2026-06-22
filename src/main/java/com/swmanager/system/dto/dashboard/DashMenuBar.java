package com.swmanager.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대시보드 메뉴별 사용 TOP 막대(@Getter — Thymeleaf {@code ${m.menu}/${m.cnt}/${m.w}} 호환).
 * 기존 컨트롤러-로컬 HashMap 을 대체(§6-4 dashboard-typed-model). menu 는 repo Object[] 원형 보존. w=막대너비%.
 */
@Getter
@AllArgsConstructor
public class DashMenuBar {
    private final Object menu;
    private final long cnt;
    private final int w;
}
