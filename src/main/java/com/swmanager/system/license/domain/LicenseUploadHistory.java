package com.swmanager.system.license.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 라이선스 대장 업로드 이력
 */
@Entity
@Table(name = "license_upload_history")
@Data
public class LicenseUploadHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime uploadDate;  // 업로드 날짜
    
    @Column(length = 500)
    private String fileName;  // 파일명
    
    @Column(nullable = false)
    private Integer totalCount;  // 전체 건수
    
    @Column(nullable = false)
    private Integer successCount;  // 성공 건수
    
    @Column
    private Integer failCount = 0;  // 실패 건수
    
    @Column(length = 100)
    private String uploadedBy;  // 업로드자
    
    @Column(nullable = false)
    private LocalDateTime createdDate;  // 생성일
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
