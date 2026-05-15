package com.swmanager.system.repository;

import com.swmanager.system.domain.InspectQrBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InspectQrBatchRepository extends JpaRepository<InspectQrBatch, Long> {

    Optional<InspectQrBatch> findByPayloadId(String payloadId);
}
