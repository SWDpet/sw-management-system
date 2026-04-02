package com.swmanager.system.repository.workplan;

import com.swmanager.system.domain.workplan.InspectIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectIssueRepository extends JpaRepository<InspectIssue, Integer> {

    List<InspectIssue> findByDocument_DocIdOrderByIssueYearDescIssueMonthDescIssueDayDesc(Integer docId);
}
