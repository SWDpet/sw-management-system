package com.swmanager.system.repository;

import com.swmanager.system.domain.PjtEquip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PjtEquipRepository extends JpaRepository<PjtEquip, Long> {

    List<PjtEquip> findByProject_ProjIdOrderBySortOrder(Long projId);

    List<PjtEquip> findByProject_ProjIdAndEquipTypeOrderBySortOrder(Long projId, String equipType);

    void deleteByProject_ProjId(Long projId);
}
