package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.ContractParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractParticipantRepository extends JpaRepository<ContractParticipant, Integer> {

    List<ContractParticipant> findByContract_ContractIdOrderBySortOrder(Integer contractId);

    List<ContractParticipant> findByUser_UserSeq(Long userSeq);

    // 현장대리인 조회
    ContractParticipant findByContract_ContractIdAndIsSiteRepTrue(Integer contractId);
}
