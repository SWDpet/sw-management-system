package com.swmanager.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 대시보드 임박 업무 일정(@Getter — Thymeleaf {@code ${m.title}/${m.date}/${m.dday}} 호환).
 * 기존 컨트롤러-로컬 HashMap 을 대체(§6-4 dashboard-typed-model). date 는 LocalDate(#temporals.format).
 */
@Getter
@AllArgsConstructor
public class DashUpcoming {
    private final String title;
    private final LocalDate date;
    private final long dday;
}
