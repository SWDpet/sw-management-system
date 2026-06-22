package com.swmanager.system.dto.dashboard;

/**
 * 연도별 사업 수 — SwProjectRepository.countByYear JPQL projection 인터페이스.
 *
 * 기존 {@code List<Map<String,Object>>} 을 Spring Data interface projection 으로 대체한다
 * (§6-4 dashboard-typed-model). JPQL `p.year AS y, COUNT(p) AS c` → getY/getC.
 * (표시용 막대높이 h 는 컨트롤러 계산 → {@link DashYearBar} 로 조립)
 */
public interface YearCountRow {
    Integer getY();
    Long getC();
}
