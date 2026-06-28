package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.DocumentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentDetailRepository extends JpaRepository<DocumentDetail, Integer> {
}
