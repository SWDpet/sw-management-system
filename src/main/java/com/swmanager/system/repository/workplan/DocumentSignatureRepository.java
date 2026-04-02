package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, Integer> {

    List<DocumentSignature> findByDocument_DocId(Integer docId);
}
