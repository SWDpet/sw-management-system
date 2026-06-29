package com.swmanager.system.lsa.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * LSA(라이선스 발급 대장) 엔티티 — lsa_license 테이블.
 * 도메인 순수성: 상위 계층(dto/service/controller) 의존 금지(ArchUnit).
 */
@Entity
@Table(name = "lsa_license")
@Getter
@Setter
public class Lsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // id DEFAULT nextval('lsa_license_id_seq')
    private Long id;

    @Column(name = "city_nm")   private String cityNm;   // 시도
    @Column(name = "dist_nm")   private String distNm;   // 시군구
    @Column(name = "dept_nm")   private String deptNm;   // 부서
    @Column(name = "team_nm")   private String teamNm;   // 팀
    @Column(name = "user_nm")   private String userNm;   // 담당자 이름
    @Column(name = "tel")       private String tel;      // 전화
    @Column(name = "email")     private String email;    // 이메일
    @Column(name = "version")   private String version;  // 버전
    @Column(name = "issuer")    private String issuer;   // 발급자

    @Column(name = "ps_info_id") private Long psInfoId;  // ps_info FK (P3 연동)

    @Column(name = "created_by") private String createdBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_by") private String updatedBy;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
