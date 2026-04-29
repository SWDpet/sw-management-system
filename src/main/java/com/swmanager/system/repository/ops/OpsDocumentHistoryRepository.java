package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsDocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpsDocumentHistoryRepository extends JpaRepository<OpsDocumentHistory, Long> {
    List<OpsDocumentHistory> findByDocument_DocIdOrderByCreatedAtDesc(Long docId);
}
