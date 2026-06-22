package com.swmanager.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대시보드 만료 임박 라이선스(@Getter — Thymeleaf {@code ${m.name}/${m.type}/${m.days}} 호환).
 * 기존 컨트롤러-로컬 HashMap 을 대체(§6-4 dashboard-typed-model).
 */
@Getter
@AllArgsConstructor
public class DashExpiring {
    private final String name;
    private final String type;
    private final long days;
}
