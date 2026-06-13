package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 외부업체 마스터 (tb_partner) — ops-fault-support M2/Step 3.
 * 요청자(업체담당자)·협력업체 양쪽 역할의 업체 디렉터리.
 */
@Entity
@Table(name = "tb_partner")
@Getter @Setter
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "partner_type", length = 20)
    private String partnerType;   // 사업단/유지보수/DB/SI/기타

    @Column(name = "biz_no", length = 20)
    private String bizNo;

    @Column(name = "main_tel", length = 40)
    private String mainTel;

    @Column(length = 1000)
    private String note;

    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartnerContact> contacts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (useYn == null) useYn = "Y";
    }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }
}
