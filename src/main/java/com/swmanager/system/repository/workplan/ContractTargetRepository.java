package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.ContractTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTargetRepository extends JpaRepository<ContractTarget, Integer> {

    List<ContractTarget> findByContract_ContractIdOrderBySortOrder(Integer contractId);
}
