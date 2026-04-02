package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Integer> {

    List<DocumentHistory> findByDocument_DocIdOrderByCreatedAtDesc(Integer docId);
}
