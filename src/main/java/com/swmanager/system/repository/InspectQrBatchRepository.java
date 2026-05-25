package com.swmanager.system.repository;

import com.swmanager.system.domain.InspectQrBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InspectQrBatchRepository extends JpaRepository<InspectQrBatch, Long> {

    Optional<InspectQrBatch> findByPayloadId(String payloadId);

    @Modifying
    @Transactional
    @Query("DELETE FROM InspectQrBatch b WHERE b.reportId = :reportId")
    void deleteByReportId(@Param("reportId") Long reportId);
}
