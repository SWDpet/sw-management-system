package com.swmanager.system.repository;

import com.swmanager.system.domain.workplan.PjtManpowerPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PjtManpowerPlanRepository extends JpaRepository<PjtManpowerPlan, Long> {

    List<PjtManpowerPlan> findByProjIdOrderBySortOrderAsc(Long projId);

    @Transactional
    void deleteByProjId(Long projId);
}
