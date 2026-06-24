package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.constant.enums.WorkPlanType;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.PerformanceSummary;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.i18n.MessageResolver;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;
import com.swmanager.system.repository.workplan.PerformanceSummaryRepository;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PerformanceService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 월간 집계(safeCount null·INSPECT 필터·정시 준수 분기)·집계 수학(null-safe·rate·0분모)·
 * 일괄/조회 위임을 커버. 필드 주입이라 ReflectionTestUtils 로 mock 주입.
 */
class PerformanceServiceTest {

    private final PerformanceSummaryRepository summaryRepository = mock(PerformanceSummaryRepository.class);
    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final OpsDocumentRepository opsDocumentRepository = mock(OpsDocumentRepository.class);
    private final WorkPlanRepository workPlanRepository = mock(WorkPlanRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MessageResolver messages = mock(MessageResolver.class);

    private PerformanceService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceService();
        ReflectionTestUtils.setField(service, "summaryRepository", summaryRepository);
        ReflectionTestUtils.setField(service, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(service, "opsDocumentRepository", opsDocumentRepository);
        ReflectionTestUtils.setField(service, "workPlanRepository", workPlanRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "messages", messages);
        when(summaryRepository.save(any(PerformanceSummary.class))).thenAnswer(i -> i.getArgument(0));
    }

    private User user(long seq) {
        User u = new User();
        u.setUserSeq(seq);
        return u;
    }

    private WorkPlan plan(String type, String status, LocalDate endDate, LocalDateTime updatedAt) {
        WorkPlan p = new WorkPlan();
        p.setPlanType(type);
        p.setStatus(status);
        p.setEndDate(endDate);
        p.setUpdatedAt(updatedAt);
        return p;
    }

    private PerformanceSummary summary(Integer install, Integer patch, Integer fault, Integer support,
                                       Integer interim, Integer completion,
                                       Integer inspectPlan, Integer inspectDone,
                                       Integer planTotal, Integer planOntime) {
        PerformanceSummary s = new PerformanceSummary();
        s.setInstallCount(install); s.setPatchCount(patch); s.setFaultCount(fault); s.setSupportCount(support);
        s.setInterimCount(interim); s.setCompletionCount(completion);
        s.setInspectPlanCount(inspectPlan); s.setInspectDoneCount(inspectDone);
        s.setPlanTotalCount(planTotal); s.setPlanOntimeCount(planOntime);
        return s;
    }

    // ===== calculateMonthlyPerformance =====

    @Test
    void calculateMonthlyPerformance_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(messages.get(eq("error.user.not_found"), any())).thenReturn("사용자 없음: 99");
        assertThatThrownBy(() -> service.calculateMonthlyPerformance(99L, 2026, 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void calculateMonthlyPerformance_newSummary_aggregatesCountsInspectAndOntime() {
        long userId = 7L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(summaryRepository.findByUser_UserSeqAndPeriodYearAndPeriodMonth(userId, 2026, 6))
                .thenReturn(Optional.empty());   // 신규 생성 경로

        // 운영문서 카운트 — PATCH 는 null 로 safeCount(null)→0 검증
        when(opsDocumentRepository.countCompletedByTypeAndUser(eq(OpsDocType.INSTALL), eq(userId), any(), any())).thenReturn(2L);
        when(opsDocumentRepository.countCompletedByTypeAndUser(eq(OpsDocType.PATCH),   eq(userId), any(), any())).thenReturn(null);
        when(opsDocumentRepository.countCompletedByTypeAndUser(eq(OpsDocType.FAULT),   eq(userId), any(), any())).thenReturn(1L);
        when(opsDocumentRepository.countCompletedByTypeAndUser(eq(OpsDocType.SUPPORT), eq(userId), any(), any())).thenReturn(3L);
        when(documentRepository.countApprovedByTypeAndUser(eq(DocumentType.INTERIM),     eq(userId), any(), any())).thenReturn(5L);
        when(documentRepository.countApprovedByTypeAndUser(eq(DocumentType.COMPLETION), eq(userId), any(), any())).thenReturn(null);

        // 업무 목록: INSPECT 계획 2건(완료 1) + SUPPORT 완료 2건(정시 1, 지연 1)
        List<WorkPlan> plans = List.of(
                plan(WorkPlanType.INSPECT.name(), WorkPlanStatus.COMPLETED.name(), LocalDate.of(2026, 6, 10), LocalDateTime.of(2026, 6, 9, 0, 0)),  // inspect done + ontime
                plan(WorkPlanType.INSPECT.name(), WorkPlanStatus.PLANNED.name(),   LocalDate.of(2026, 6, 20), LocalDateTime.of(2026, 6, 1, 0, 0)),  // inspect plan only
                plan(WorkPlanType.SUPPORT.name(), WorkPlanStatus.COMPLETED.name(), null,                     LocalDateTime.of(2026, 6, 30, 0, 0)),                  // ontime (endDate null)
                plan(WorkPlanType.SUPPORT.name(), WorkPlanStatus.COMPLETED.name(), LocalDate.of(2026, 6, 5), LocalDateTime.of(2026, 6, 20, 0, 0))                    // 지연 → ontime 제외
        );
        when(workPlanRepository.findByAssigneeAndDateRange(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(plans);

        PerformanceSummary out = service.calculateMonthlyPerformance(userId, 2026, 6);

        assertThat(out.getInstallCount()).isEqualTo(2);
        assertThat(out.getPatchCount()).isZero();          // null → 0
        assertThat(out.getFaultCount()).isEqualTo(1);
        assertThat(out.getSupportCount()).isEqualTo(3);
        assertThat(out.getInterimCount()).isEqualTo(5);
        assertThat(out.getCompletionCount()).isZero();     // null → 0
        assertThat(out.getInspectPlanCount()).isEqualTo(2);
        assertThat(out.getInspectDoneCount()).isEqualTo(1);
        assertThat(out.getPlanTotalCount()).isEqualTo(4);
        assertThat(out.getPlanOntimeCount()).isEqualTo(2); // 정시 2(지연 1 제외)
        assertThat(out.getCalculatedAt()).isNotNull();
        assertThat(out.getPeriodYear()).isEqualTo(2026);
        assertThat(out.getPeriodMonth()).isEqualTo(6);
        verify(summaryRepository).save(out);
    }

    @Test
    void calculateMonthlyPerformance_reusesExistingSummary() {
        long userId = 8L;
        PerformanceSummary existing = new PerformanceSummary();
        existing.setSummaryId(123);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(summaryRepository.findByUser_UserSeqAndPeriodYearAndPeriodMonth(userId, 2026, 5))
                .thenReturn(Optional.of(existing));
        when(opsDocumentRepository.countCompletedByTypeAndUser(any(), eq(userId), any(), any())).thenReturn(0L);
        when(documentRepository.countApprovedByTypeAndUser(any(), eq(userId), any(), any())).thenReturn(0L);
        when(workPlanRepository.findByAssigneeAndDateRange(eq(userId), any(), any())).thenReturn(List.of());

        PerformanceSummary out = service.calculateMonthlyPerformance(userId, 2026, 5);

        assertThat(out).isSameAs(existing);                // 기존 요약 재사용
        assertThat(out.getSummaryId()).isEqualTo(123);
    }

    @Test
    void calculateAllUsersMonthlyPerformance_iteratesEnabledUsers_returnsCount() {
        when(userRepository.findByEnabledTrue()).thenReturn(List.of(user(1L), user(2L)));
        when(userRepository.findById(anyLong())).thenAnswer(i -> Optional.of(user(i.getArgument(0))));
        when(summaryRepository.findByUser_UserSeqAndPeriodYearAndPeriodMonth(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(opsDocumentRepository.countCompletedByTypeAndUser(any(), anyLong(), any(), any())).thenReturn(0L);
        when(documentRepository.countApprovedByTypeAndUser(any(), anyLong(), any(), any())).thenReturn(0L);
        when(workPlanRepository.findByAssigneeAndDateRange(anyLong(), any(), any())).thenReturn(List.of());

        int count = service.calculateAllUsersMonthlyPerformance(2026, 6);

        assertThat(count).isEqualTo(2);
        verify(summaryRepository, times(2)).save(any(PerformanceSummary.class));
    }

    // ===== 조회 위임 =====

    @Test
    void getPersonalYearlyPerformance_delegates() {
        List<PerformanceSummary> list = List.of(new PerformanceSummary());
        when(summaryRepository.findByUser_UserSeqAndPeriodYearOrderByPeriodMonthAsc(5L, 2026)).thenReturn(list);
        assertThat(service.getPersonalYearlyPerformance(5L, 2026)).isSameAs(list);
    }

    @Test
    void getPersonalPerformanceRange_encodesYearMonthAndDelegates() {
        when(summaryRepository.findByUserAndPeriodRange(5L, 202603, 202608)).thenReturn(List.of());
        service.getPersonalPerformanceRange(5L, 2026, 3, 2026, 8);
        verify(summaryRepository).findByUserAndPeriodRange(5L, 202603, 202608);   // year*100+month
    }

    @Test
    void getDeptMonthlyPerformance_delegates() {
        List<PerformanceSummary> list = List.of(new PerformanceSummary());
        when(summaryRepository.findByPeriodYearAndPeriodMonthOrderByUser_UsernameAsc(2026, 6)).thenReturn(list);
        assertThat(service.getDeptMonthlyPerformance(2026, 6)).isSameAs(list);
    }

    // ===== getAggregatedPerformance =====

    @Test
    void getAggregatedPerformance_sumsNullSafe_andComputesRates() {
        PerformanceSummary s1 = summary(2, 1, 0, 1, 3, 1, 4, 2, 10, 8);
        PerformanceSummary s2 = summary(null, null, null, null, null, null, null, null, null, null); // null-safe 분기
        when(summaryRepository.findByUserAndPeriodRange(5L, 202601, 202612)).thenReturn(List.of(s1, s2));

        Map<String, Object> r = service.getAggregatedPerformance(5L, 2026, 1, 2026, 12);

        assertThat(r.get("installTotal")).isEqualTo(2);
        assertThat(r.get("patchTotal")).isEqualTo(1);
        assertThat(r.get("supportTotal")).isEqualTo(1);
        assertThat(r.get("interimTotal")).isEqualTo(3);
        assertThat(r.get("completionTotal")).isEqualTo(1);
        assertThat(r.get("inspectRate")).isEqualTo(50L);   // round(2*100/4)
        assertThat(r.get("ontimeRate")).isEqualTo(80L);    // round(8*100/10)
        assertThat(r.get("details")).isEqualTo(List.of(s1, s2));
    }

    @Test
    void getAggregatedPerformance_emptyList_zeroRates_noDivByZero() {
        when(summaryRepository.findByUserAndPeriodRange(5L, 202601, 202601)).thenReturn(List.of());
        Map<String, Object> r = service.getAggregatedPerformance(5L, 2026, 1, 2026, 1);
        assertThat(r.get("installTotal")).isEqualTo(0);
        assertThat(r.get("inspectRate")).isEqualTo(0L);    // 분모 0 → 0
        assertThat(r.get("ontimeRate")).isEqualTo(0L);
    }
}
