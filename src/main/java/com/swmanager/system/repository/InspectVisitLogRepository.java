package com.swmanager.system.repository;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.domain.InspectVisitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InspectVisitLogRepository extends JpaRepository<InspectVisitLog, Long> {

    List<InspectVisitLog> findByReportIdOrderBySortOrderAsc(Long reportId);

    @Modifying
    @Transactional
    @Query("DELETE FROM InspectVisitLog v WHERE v.reportId = :reportId")
    void deleteByReportId(@Param("reportId") Long reportId);

    /**
     * 같은 프로젝트의 이전 월(currentMonth 미만) COMPLETED 상태 보고서들의 방문이력을 조회
     * @param pjtId 프로젝트 ID
     * @param currentMonth 현재 점검월 (YYYY-MM 형식), 이 월 미만의 이력만 조회
     * @return 연도/월/일 순 정렬된 방문이력
     */
    @Query("SELECT v FROM InspectVisitLog v " +
           "WHERE v.reportId IN (" +
           "  SELECT r.id FROM InspectReport r " +
           "  WHERE r.pjtId = :pjtId " +
           "    AND r.status = :status " +
           "    AND r.inspectMonth < :currentMonth" +
           ") " +
           "ORDER BY v.visitYear ASC, v.visitMonth ASC, v.visitDay ASC, v.sortOrder ASC")
    List<InspectVisitLog> findPreviousVisitsByProject(@Param("pjtId") Long pjtId,
                                                      @Param("currentMonth") String currentMonth,
                                                      @Param("status") DocumentStatus status);

    default List<InspectVisitLog> findPreviousVisitsByProject(Long pjtId, String currentMonth) {
        return findPreviousVisitsByProject(pjtId, currentMonth, DocumentStatus.COMPLETED);
    }
}
