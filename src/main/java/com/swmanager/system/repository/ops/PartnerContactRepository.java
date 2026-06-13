package com.swmanager.system.repository.ops;

import com.swmanager.system.domain.ops.PartnerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerContactRepository extends JpaRepository<PartnerContact, Long> {

    List<PartnerContact> findByPartner_PartnerIdAndUseYnOrderByNameAsc(Long partnerId, String useYn);

    /** 요청자(업체담당자) 검색 — 담당자명 또는 업체명 LIKE (활성). */
    @Query("SELECT c FROM PartnerContact c WHERE c.useYn = 'Y' AND c.partner.useYn = 'Y' " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "  OR LOWER(c.partner.name) LIKE LOWER(CONCAT('%', :kw, '%'))) ORDER BY c.name")
    List<PartnerContact> searchActive(@Param("kw") String kw);
}
