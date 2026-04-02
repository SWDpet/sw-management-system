package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.InspectChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectChecklistRepository extends JpaRepository<InspectChecklist, Integer> {

    List<InspectChecklist> findByDocument_DocIdOrderBySortOrder(Integer docId);

    List<InspectChecklist> findByDocument_DocIdAndInspectMonthOrderBySortOrder(Integer docId, Integer inspectMonth);
}
