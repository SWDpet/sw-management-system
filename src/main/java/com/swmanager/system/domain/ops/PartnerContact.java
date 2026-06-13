package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 외부업체 담당자 (tb_partner_contact) — ops-fault-support M2/Step 3.
 * 요청자(업체담당자) 후보. tb_ops_doc.requester_contact_id 가 이 PK 참조.
 */
@Entity
@Table(name = "tb_partner_contact")
@Getter @Setter
public class PartnerContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long contactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String position;

    @Column(length = 40)
    private String tel;

    @Column(length = 100)
    private String email;

    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
