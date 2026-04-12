package com.swmanager.system.repository;

import com.swmanager.system.domain.workplan.PjtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PjtScheduleRepository extends JpaRepository<PjtSchedule, Long> {

    List<PjtSchedule> findByProjIdOrderBySortOrderAsc(Long projId);

    @Transactional
    void deleteByProjId(Long projId);
}
