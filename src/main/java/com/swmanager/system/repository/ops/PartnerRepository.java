package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findByUseYnOrderByNameAsc(String useYn);

    List<Partner> findByUseYnAndNameContainingOrderByNameAsc(String useYn, String name);
}
