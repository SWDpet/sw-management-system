package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_document_signature")
@Getter @Setter
public class DocumentSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sign_id")
    private Integer signId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Column(name = "signer_type", nullable = false, length = 30)
    private String signerType; // INSPECTOR, CONFIRMER, REPRESENTATIVE

    @Column(name = "signer_name", nullable = false, length = 50)
    private String signerName;

    @Column(name = "signer_org", length = 200)
    private String signerOrg;

    @Column(name = "signature_image", length = 500)
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
