package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.DocumentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentDetailRepository extends JpaRepository<DocumentDetail, Integer> {

    List<DocumentDetail> findByDocument_DocIdOrderBySortOrder(Integer docId);

    Optional<DocumentDetail> findByDocument_DocIdAndSectionKey(Integer docId, String sectionKey);
}
