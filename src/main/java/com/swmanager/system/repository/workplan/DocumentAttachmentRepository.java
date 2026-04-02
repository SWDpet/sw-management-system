package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.DocumentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAttachmentRepository extends JpaRepository<DocumentAttachment, Integer> {

    List<DocumentAttachment> findByDocument_DocId(Integer docId);
}
