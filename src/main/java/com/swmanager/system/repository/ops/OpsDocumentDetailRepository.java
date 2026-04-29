package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsDocumentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpsDocumentDetailRepository extends JpaRepository<OpsDocumentDetail, Long> {
    List<OpsDocumentDetail> findByDocument_DocIdOrderBySortOrderAsc(Long docId);
    void deleteByDocument_DocId(Long docId);
}
