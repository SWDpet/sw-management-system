package com.swmanager.system.geonuris.repository;

import com.swmanager.system.geonuris.domain.GeonurisLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GeonurisLicenseRepository extends JpaRepository<GeonurisLicense, Long> {

    /** 전체 최신순 */
    List<GeonurisLicense> findAllByOrderByCreatedAtDesc();

    /** 라이선스 종류별 최신순 */
    List<GeonurisLicense> findByLicenseTypeOrderByCreatedAtDesc(String licenseType);

    /** 키워드 검색 (사용자, 기관, 이메일, MAC) */
    @Query("SELECT g FROM GeonurisLicense g WHERE " +
           "LOWER(COALESCE(g.userName,'')) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(COALESCE(g.organization,'')) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(COALESCE(g.email,'')) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(COALESCE(g.macAddress,'')) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(COALESCE(g.issuer,'')) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "ORDER BY g.createdAt DESC")
    List<GeonurisLicense> searchByKeyword(@Param("kw") String keyword);

    /** 타입별 건수 */
    long countByLicenseType(String licenseType);
}
