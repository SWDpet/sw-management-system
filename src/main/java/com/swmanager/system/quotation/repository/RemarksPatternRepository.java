package com.swmanager.system.quotation.repository;

import com.swmanager.system.quotation.domain.RemarksPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RemarksPatternRepository extends JpaRepository<RemarksPattern, Long> {

    List<RemarksPattern> findAllByOrderBySortOrderAscPatternNameAsc();
}
