package com.swmanager.system.repository;

import com.swmanager.system.domain.InspectCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InspectCheckResultRepository extends JpaRepository<InspectCheckResult, Long> {

    List<InspectCheckResult> findByReportIdOrderBySectionAscSortOrderAsc(Long reportId);

    @Modifying
    @Transactional
    @Query("DELETE FROM InspectCheckResult c WHERE c.reportId = :reportId")
    void deleteByReportId(@Param("reportId") Long reportId);

    /** inspection-report-d-v5: 자동 섹션만 부분 삭제 (수동 영역 보존용). */
    @Modifying
    @Transactional
    @Query("DELETE FROM InspectCheckResult c WHERE c.reportId = :reportId AND c.section IN :sections")
    void deleteByReportIdAndSectionIn(@Param("reportId") Long reportId, @Param("sections") List<String> sections);
}
