package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsDocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpsDocumentSignatureRepository extends JpaRepository<OpsDocumentSignature, Long> {
    List<OpsDocumentSignature> findByDocument_DocIdOrderByCreatedAtAsc(Long docId);
}
