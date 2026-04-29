package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ops_doc_signature")
@Getter @Setter
public class OpsDocumentSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sign_id")
    private Long signId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private OpsDocument document;

    @Column(name = "signer_type", nullable = false, length = 30)
    private String signerType;

    @Column(name = "signer_name", nullable = false, length = 50)
    private String signerName;

    @Column(name = "signer_org", length = 200)
    private String signerOrg;

    /** Base64 PNG (DB §A 권고: 점검 패턴 흡수 위해 TEXT). */
    @Column(name = "signature_image", columnDefinition = "TEXT")
    private String signatureImage;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
