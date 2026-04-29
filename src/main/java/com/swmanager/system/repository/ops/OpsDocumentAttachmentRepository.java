package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsDocumentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpsDocumentAttachmentRepository extends JpaRepository<OpsDocumentAttachment, Long> {
    List<OpsDocumentAttachment> findByDocument_DocIdOrderByUploadedAtDesc(Long docId);
}
