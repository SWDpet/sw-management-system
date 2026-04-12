package com.swmanager.system.repository;

import com.swmanager.system.domain.workplan.PjtTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PjtTargetRepository extends JpaRepository<PjtTarget, Long> {

    List<PjtTarget> findByProjIdOrderBySortOrderAsc(Long projId);

    @Transactional
    void deleteByProjId(Long projId);
}
