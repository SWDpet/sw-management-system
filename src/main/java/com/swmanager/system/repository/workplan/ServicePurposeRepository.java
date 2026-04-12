package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.ServicePurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicePurposeRepository extends JpaRepository<ServicePurpose, Integer> {

    List<ServicePurpose> findBySysNmEnAndUseYnOrderBySortOrder(String sysNmEn, String useYn);

    List<ServicePurpose> findBySysNmEnAndPurposeTypeAndUseYnOrderBySortOrder(
            String sysNmEn, String purposeType, String useYn);
}
