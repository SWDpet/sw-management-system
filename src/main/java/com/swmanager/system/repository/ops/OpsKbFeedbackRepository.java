package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsKbFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpsKbFeedbackRepository extends JpaRepository<OpsKbFeedback, Long> {
}
