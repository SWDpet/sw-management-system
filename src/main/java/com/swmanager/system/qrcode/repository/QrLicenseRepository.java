package com.swmanager.system.qrcode.repository;

import com.swmanager.system.qrcode.domain.QrLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QrLicenseRepository extends JpaRepository<QrLicense, Long> {

    List<QrLicense> findAllByOrderByIssuedAtDesc();

    @Query("SELECT q FROM QrLicense q WHERE " +
           "LOWER(q.endUserName) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.endUserNameKo) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.products) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.productsKo) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.serialNumber) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(q.applicationName) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "ORDER BY q.issuedAt DESC")
    List<QrLicense> searchByKeyword(@Param("kw") String keyword);
}
