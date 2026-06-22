package com.swmanager.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대시보드 연도별 추이 막대(@Getter — Thymeleaf {@code ${r.y}/${r.c}/${r.h}} 호환).
 * 기존 컨트롤러-로컬 HashMap(y/c/h) 을 대체(§6-4 dashboard-typed-model). h=막대높이(20~100%).
 */
@Getter
@AllArgsConstructor
public class DashYearBar {
    private final Integer y;
    private final long c;
    private final int h;
}
