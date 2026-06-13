package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.OpsDocPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OpsDocPartnerRepository extends JpaRepository<OpsDocPartner, OpsDocPartner.PK> {

    List<OpsDocPartner> findByDocId(Long docId);

    @Transactional
    void deleteByDocId(Long docId);
}
