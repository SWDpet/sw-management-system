package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.ContractParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractParticipantRepository extends JpaRepository<ContractParticipant, Integer> {

    List<ContractParticipant> findByProject_ProjIdOrderBySortOrder(Long projId);

    List<ContractParticipant> findByProject_ProjIdAndIsSiteRepTrue(Long projId);
}
