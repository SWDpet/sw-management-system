package com.swmanager.system.license.repository;

import com.swmanager.system.license.domain.LicenseUploadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseUploadHistoryRepository extends JpaRepository<LicenseUploadHistory, Long> {
    
    /**
     * 최근 업로드 이력 조회 (최대 10개)
     */
    List<LicenseUploadHistory> findTop10ByOrderByUploadDateDesc();
    
    /**
     * 업로드자별 조회
     */
    List<LicenseUploadHistory> findByUploadedByOrderByUploadDateDesc(String uploadedBy);
}
