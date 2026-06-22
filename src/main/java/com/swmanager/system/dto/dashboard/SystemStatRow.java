package com.swmanager.system.dto.dashboard;

/**
 * 대시보드 시스템별 통계 행 — SwProjectRepository.getSystemStats JPQL projection 인터페이스.
 *
 * 기존 {@code List<Map<String,Object>>} 응답을 Spring Data interface projection 으로 대체한다
 * (§6-4 dashboard-typed-model). JPQL alias(sysNm…progCnt)가 getter(getSysNm…getProgCnt)로 매핑.
 * Thymeleaf {@code ${row.compCnt}} 등 getter 속성접근 호환(record 불가 — 백지 렌더 방지).
 * 집계 타입은 Hibernate 6 기준 wrapper Long(COUNT/SUM).
 */
public interface SystemStatRow {
    String getSysNm();
    String getSysNmEn();
    Long getTotalCnt();
    Long getSumCont();
    Long getSumSw();
    Long getSumCnslt();
    Long getSumDb();
    Long getSumPkg();
    Long getSumDev();
    Long getSumHw();
    Long getSumEtc();
    Long getCompCnt();
    Long getProgCnt();
}
