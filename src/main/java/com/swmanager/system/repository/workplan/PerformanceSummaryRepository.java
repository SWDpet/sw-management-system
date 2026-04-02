package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.PerformanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceSummaryRepository extends JpaRepository<PerformanceSummary, Integer> {

    // 개인 월별 성과 조회
    Optional<PerformanceSummary> findByUser_UserSeqAndPeriodYearAndPeriodMonth(
            Long userSeq, Integer periodYear, Integer periodMonth);

    // 개인 기간별 성과 조회
    List<PerformanceSummary> findByUser_UserSeqAndPeriodYearOrderByPeriodMonthAsc(
            Long userSeq, Integer periodYear);

    // 개인 기간 범위 성과 조회
    @Query("SELECT ps FROM PerformanceSummary ps " +
           "WHERE ps.user.userSeq = :userId " +
           "AND (ps.periodYear * 100 + ps.periodMonth) BETWEEN :fromYm AND :toYm " +
           "ORDER BY ps.periodYear ASC, ps.periodMonth ASC")
    List<PerformanceSummary> findByUserAndPeriodRange(
            @Param("userId") Long userId,
            @Param("fromYm") Integer fromYm,
            @Param("toYm") Integer toYm);

    // 부서 전체 월간 성과 (팀원 비교)
    List<PerformanceSummary> findByPeriodYearAndPeriodMonthOrderByUser_UsernameAsc(
            Integer periodYear, Integer periodMonth);
}
