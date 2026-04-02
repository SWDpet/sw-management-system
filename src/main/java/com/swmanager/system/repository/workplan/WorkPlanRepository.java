package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.WorkPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkPlanRepository extends JpaRepository<WorkPlan, Integer> {

    // 캘린더 뷰: 기간별 업무 조회
    @Query("SELECT wp FROM WorkPlan wp " +
           "WHERE wp.startDate <= :endDate AND (wp.endDate >= :startDate OR wp.endDate IS NULL) " +
           "ORDER BY wp.startDate ASC")
    List<WorkPlan> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 담당자별 캘린더 조회
    @Query("SELECT wp FROM WorkPlan wp " +
           "WHERE wp.assignee.userSeq = :assigneeId " +
           "AND wp.startDate <= :endDate AND (wp.endDate >= :startDate OR wp.endDate IS NULL) " +
           "ORDER BY wp.startDate ASC")
    List<WorkPlan> findByAssigneeAndDateRange(
            @Param("assigneeId") Long assigneeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 인프라별 업무 목록
    List<WorkPlan> findByInfra_InfraIdOrderByStartDateDesc(Long infraId);

    // 프로세스 단계별 조회 (칸반 보드)
    List<WorkPlan> findByProcessStepAndStatusNotInOrderByStartDateAsc(
            Integer processStep, List<String> excludeStatuses);

    // 사전연락 일정 조회 (알림용)
    @Query("SELECT wp FROM WorkPlan wp " +
           "WHERE wp.planType = 'PRE_CONTACT' " +
           "AND wp.status = 'PLANNED' " +
           "AND wp.startDate = :targetDate")
    List<WorkPlan> findPreContactsByDate(@Param("targetDate") LocalDate targetDate);

    // 부모 플랜의 자식 조회
    List<WorkPlan> findByParentPlan_PlanId(Integer parentPlanId);

    // 유형별 조회 (점검 방문 현황용)
    List<WorkPlan> findByPlanTypeInOrderByStartDateDesc(List<String> planTypes);

    // 유형별 + 상태별 복합 검색
    @Query("SELECT wp FROM WorkPlan wp " +
           "WHERE (:type IS NULL OR wp.planType = :type) " +
           "AND (:status IS NULL OR wp.status = :status) " +
           "AND (:assigneeId IS NULL OR wp.assignee.userSeq = :assigneeId) " +
           "AND (:infraId IS NULL OR wp.infra.infraId = :infraId)")
    Page<WorkPlan> searchWorkPlans(
            @Param("type") String type,
            @Param("status") String status,
            @Param("assigneeId") Long assigneeId,
            @Param("infraId") Long infraId,
            Pageable pageable);
}
