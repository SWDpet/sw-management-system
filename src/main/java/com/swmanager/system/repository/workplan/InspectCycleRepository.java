package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.InspectCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectCycleRepository extends JpaRepository<InspectCycle, Integer> {

    List<InspectCycle> findByInfra_InfraId(Long infraId);

    List<InspectCycle> findByAssignee_UserSeqAndIsActiveTrue(Long userSeq);

    List<InspectCycle> findByIsActiveTrueOrderByInfra_CityNmAscInfra_DistNmAsc();

    List<InspectCycle> findByCycleTypeAndIsActiveTrue(String cycleType);
}
