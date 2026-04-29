package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.constant.enums.WorkPlanType;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.PerformanceSummary;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;
import com.swmanager.system.repository.workplan.PerformanceSummaryRepository;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import com.swmanager.system.i18n.MessageResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PerformanceService {

    @Autowired private PerformanceSummaryRepository summaryRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private OpsDocumentRepository opsDocumentRepository;  // doc-split-ops: 운영문서 4 종 카운트
    @Autowired private WorkPlanRepository workPlanRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MessageResolver messages;

    /**
     * 특정 사용자의 월간 성과 집계 (배치/실시간)
     */
    public PerformanceSummary calculateMonthlyPerformance(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.user.not_found", userId)));

        PerformanceSummary summary = summaryRepository
                .findByUser_UserSeqAndPeriodYearAndPeriodMonth(userId, year, month)
                .orElseGet(() -> {
                    PerformanceSummary s = new PerformanceSummary();
                    s.setUser(user);
                    s.setPeriodYear(year);
                    s.setPeriodMonth(month);
                    return s;
                });

        LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        // 문서 유형별 승인 건수 집계
        // doc-split-ops: 사업문서 (INTERIM/COMPLETION) 는 tb_document, 운영문서 (INSTALL/PATCH/FAULT/SUPPORT) 는 tb_ops_doc.
        summary.setInstallCount(safeCount(opsDocumentRepository.countCompletedByTypeAndUser(OpsDocType.INSTALL, userId, from, to)));
        summary.setPatchCount(safeCount(opsDocumentRepository.countCompletedByTypeAndUser(OpsDocType.PATCH, userId, from, to)));
        summary.setFaultCount(safeCount(opsDocumentRepository.countCompletedByTypeAndUser(OpsDocType.FAULT, userId, from, to)));
        summary.setSupportCount(safeCount(opsDocumentRepository.countCompletedByTypeAndUser(OpsDocType.SUPPORT, userId, from, to)));
        summary.setInterimCount(safeCount(documentRepository.countApprovedByTypeAndUser(DocumentType.INTERIM, userId, from, to)));
        summary.setCompletionCount(safeCount(documentRepository.countApprovedByTypeAndUser(DocumentType.COMPLETION, userId, from, to)));

        // 정기점검 수행율
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        var allInspects = workPlanRepository.findByAssigneeAndDateRange(userId, startDate, endDate);
        // S16: Enum 위임 (리터럴 제거)
        int inspectPlan = (int) allInspects.stream().filter(p -> WorkPlanType.INSPECT.name().equals(p.getPlanType())).count();
        int inspectDone = (int) allInspects.stream().filter(p -> WorkPlanType.INSPECT.name().equals(p.getPlanType()) && WorkPlanStatus.COMPLETED.name().equals(p.getStatus())).count();
        summary.setInspectPlanCount(inspectPlan);
        summary.setInspectDoneCount(inspectDone);

        // 일정 준수율
        var allPlans = workPlanRepository.findByAssigneeAndDateRange(userId, startDate, endDate);
        int planTotal = allPlans.size();
        int planOntime = (int) allPlans.stream()
                .filter(p -> WorkPlanStatus.COMPLETED.name().equals(p.getStatus()))
                .filter(p -> p.getEndDate() == null || !p.getUpdatedAt().toLocalDate().isAfter(p.getEndDate()))
                .count();
        summary.setPlanTotalCount(planTotal);
        summary.setPlanOntimeCount(planOntime);

        summary.setCalculatedAt(LocalDateTime.now());

        return summaryRepository.save(summary);
    }

    /**
     * 전체 사용자 월간 성과 일괄 집계
     */
    public int calculateAllUsersMonthlyPerformance(int year, int month) {
        List<User> users = userRepository.findByEnabledTrue();
        int count = 0;
        for (User user : users) {
            calculateMonthlyPerformance(user.getUserSeq(), year, month);
            count++;
        }
        return count;
    }

    /**
     * 개인 연간 성과 조회
     */
    @Transactional(readOnly = true)
    public List<PerformanceSummary> getPersonalYearlyPerformance(Long userId, int year) {
        return summaryRepository.findByUser_UserSeqAndPeriodYearOrderByPeriodMonthAsc(userId, year);
    }

    /**
     * 개인 기간별 성과 조회
     */
    @Transactional(readOnly = true)
    public List<PerformanceSummary> getPersonalPerformanceRange(Long userId, int fromYear, int fromMonth, int toYear, int toMonth) {
        int fromYm = fromYear * 100 + fromMonth;
        int toYm = toYear * 100 + toMonth;
        return summaryRepository.findByUserAndPeriodRange(userId, fromYm, toYm);
    }

    /**
     * 부서 전체 월간 성과 비교
     */
    @Transactional(readOnly = true)
    public List<PerformanceSummary> getDeptMonthlyPerformance(int year, int month) {
        return summaryRepository.findByPeriodYearAndPeriodMonthOrderByUser_UsernameAsc(year, month);
    }

    /**
     * 성과 합산 (기간별 리포트용)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAggregatedPerformance(Long userId, int fromYear, int fromMonth, int toYear, int toMonth) {
        List<PerformanceSummary> list = getPersonalPerformanceRange(userId, fromYear, fromMonth, toYear, toMonth);

        Map<String, Object> result = new HashMap<>();
        result.put("installTotal", list.stream().mapToInt(s -> s.getInstallCount() != null ? s.getInstallCount() : 0).sum());
        result.put("patchTotal", list.stream().mapToInt(s -> s.getPatchCount() != null ? s.getPatchCount() : 0).sum());
        result.put("faultTotal", list.stream().mapToInt(s -> s.getFaultCount() != null ? s.getFaultCount() : 0).sum());
        result.put("supportTotal", list.stream().mapToInt(s -> s.getSupportCount() != null ? s.getSupportCount() : 0).sum());
        result.put("interimTotal", list.stream().mapToInt(s -> s.getInterimCount() != null ? s.getInterimCount() : 0).sum());
        result.put("completionTotal", list.stream().mapToInt(s -> s.getCompletionCount() != null ? s.getCompletionCount() : 0).sum());

        int inspectPlan = list.stream().mapToInt(s -> s.getInspectPlanCount() != null ? s.getInspectPlanCount() : 0).sum();
        int inspectDone = list.stream().mapToInt(s -> s.getInspectDoneCount() != null ? s.getInspectDoneCount() : 0).sum();
        result.put("inspectRate", inspectPlan > 0 ? Math.round(inspectDone * 100.0 / inspectPlan) : 0);

        int planTotal = list.stream().mapToInt(s -> s.getPlanTotalCount() != null ? s.getPlanTotalCount() : 0).sum();
        int planOntime = list.stream().mapToInt(s -> s.getPlanOntimeCount() != null ? s.getPlanOntimeCount() : 0).sum();
        result.put("ontimeRate", planTotal > 0 ? Math.round(planOntime * 100.0 / planTotal) : 0);

        result.put("details", list);
        return result;
    }

    private int safeCount(Long count) {
        return count != null ? count.intValue() : 0;
    }
}
