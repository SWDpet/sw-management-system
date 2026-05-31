package com.swmanager.system.repository;

import com.swmanager.system.domain.InspectReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectReportRepository extends JpaRepository<InspectReport, Long> {

    List<InspectReport> findByPjtIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long pjtId);

    Optional<InspectReport> findByPjtIdAndInspectMonthAndDeletedAtIsNull(Long pjtId, String inspectMonth);

    /** 직전 회차(같은 사업, 더 이전 점검월) — 점검주기 추이 윈도우 시작점 산출용. inspect_month=YYYY-MM 사전순=시간순. */
    Optional<InspectReport> findTopByPjtIdAndInspectMonthLessThanAndDeletedAtIsNullOrderByInspectMonthDesc(
            Long pjtId, String inspectMonth);

    /** inspection-qr-batch sprint: 멱등 응답용 — batch_id 는 UNIQUE (partial index where not null). */
    Optional<InspectReport> findByBatchIdAndDeletedAtIsNull(String batchId);
}
