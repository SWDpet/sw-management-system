package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsKb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpsKbRepository extends JpaRepository<OpsKb, Long> {

    List<OpsKb> findByGubun(String gubun);
}
