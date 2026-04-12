package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.ProcessMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessMasterRepository extends JpaRepository<ProcessMaster, Integer> {

    List<ProcessMaster> findBySysNmEnAndUseYnOrderBySortOrder(String sysNmEn, String useYn);
}
