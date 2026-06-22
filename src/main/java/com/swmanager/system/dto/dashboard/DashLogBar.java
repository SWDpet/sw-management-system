package com.swmanager.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대시보드 일자별 접속·활동 막대(@Getter — Thymeleaf {@code ${t.date}/${t.act}/${t.visitors}/${t.h}} 호환).
 * 기존 컨트롤러-로컬 HashMap 을 대체(§6-4 dashboard-typed-model). date 는 repo Object[] 원형 보존.
 */
@Getter
@AllArgsConstructor
public class DashLogBar {
    private final Object date;
    private final long act;
    private final long visitors;
    private final int h;
}
