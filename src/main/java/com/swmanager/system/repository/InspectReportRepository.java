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

    /** inspection-qr-batch sprint: 멱등 응답용 — batch_id 는 UNIQUE (partial index where not null). */
    Optional<InspectReport> findByBatchIdAndDeletedAtIsNull(String batchId);
}
