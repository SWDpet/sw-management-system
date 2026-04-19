package com.swmanager.system.repository;

import com.swmanager.system.domain.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {

    List<OrgUnit> findByParentIsNullAndUseYnOrderBySortOrderAsc(String useYn);

    List<OrgUnit> findByParent_UnitIdAndUseYnOrderBySortOrderAsc(Long parentId, String useYn);

    boolean existsByParent_UnitIdAndUseYn(Long parentId, String useYn);

    List<OrgUnit> findAllByUseYnOrderBySortOrderAsc(String useYn);
}
