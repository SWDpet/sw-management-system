package com.swmanager.system.license.repository;

import com.swmanager.system.license.domain.LicenseRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRegistryRepository extends JpaRepository<LicenseRegistry, Long> {

    Optional<LicenseRegistry> findByLicenseId(String licenseId);

    List<LicenseRegistry> findByProductId(String productId);
    Page<LicenseRegistry> findByProductId(String productId, Pageable pageable);

    boolean existsByLicenseId(String licenseId);
    boolean existsByLicenseIdAndProductId(String licenseId, String productId);
    Optional<LicenseRegistry> findByLicenseIdAndProductId(String licenseId, String productId);

    @Query("SELECT COUNT(l) FROM LicenseRegistry l")
    Long getTotalCount();

    List<LicenseRegistry> findAllByOrderByUploadDateDesc();

    // ★ 날짜 범위로 신규 건수 조회 (당일 00:00:00 ~ 익일 00:00:00)
    long countByUploadDateBetween(LocalDateTime start, LocalDateTime end);

    // ★ 날짜 범위로 신규 목록 조회 (최신순)
    List<LicenseRegistry> findByUploadDateBetweenOrderByIdDesc(LocalDateTime start, LocalDateTime end);

    // ★ 가장 최근 upload_date 1건 조회 (최신 업로드 날짜 확인용)
    Optional<LicenseRegistry> findTopByOrderByUploadDateDesc();

    // 검색 (페이징)
    @Query("SELECT l FROM LicenseRegistry l WHERE LOWER(l.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<LicenseRegistry> searchByFullName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT l FROM LicenseRegistry l WHERE LOWER(l.registeredTo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<LicenseRegistry> searchByRegisteredTo(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT l FROM LicenseRegistry l WHERE LOWER(l.company) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<LicenseRegistry> searchByCompany(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT l FROM LicenseRegistry l WHERE LOWER(l.hardwareId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<LicenseRegistry> searchByHardwareId(@Param("keyword") String keyword, Pageable pageable);
}
