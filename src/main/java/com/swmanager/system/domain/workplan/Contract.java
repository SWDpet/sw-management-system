package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.Infra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_contract")
@Getter @Setter
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Integer contractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    @Column(name = "contract_name", nullable = false, length = 300)
    private String contractName;

    @Column(name = "contract_no", length = 100)
    private String contractNo;

    @Column(name = "contract_type", nullable = false, length = 30)
    private String contractType; // UNIT_PRICE, PROCUREMENT, PRIVATE, SUBCONTRACT

    @Column(name = "contract_method", length = 100)
    private String contractMethod;

    @Column(name = "contract_law", length = 100)
    private String contractLaw;

    @Column(name = "contract_clause", length = 100)
    private String contractClause;

    @Column(name = "contract_amount")
    private Long contractAmount;

    @Column(name = "guarantee_amount")
    private Long guaranteeAmount;

    @Column(name = "guarantee_rate", precision = 5, scale = 2)
    private BigDecimal guaranteeRate;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "progress_status", nullable = false, length = 30)
    private String progressStatus; // BUDGET, CONTRACTING, COMMENCED, IN_PROGRESS, INTERIM, COMPLETED, SETTLED

    @Column(name = "contract_year")
    private Integer contractYear;

    // 발주처 정보
    @Column(name = "client_org", length = 200)
    private String clientOrg;

    @Column(name = "client_addr", length = 500)
    private String clientAddr;

    @Column(name = "client_phone", length = 50)
    private String clientPhone;

    @Column(name = "client_fax", length = 50)
    private String clientFax;

    @Column(name = "client_dept", length = 100)
    private String clientDept;

    @Column(name = "client_contact", length = 50)
    private String clientContact;

    @Column(name = "client_contact_phone", length = 50)
    private String clientContactPhone;

    // 하도급 정보
    @Column(name = "prime_contractor", length = 200)
    private String primeContractor;

    @Column(name = "subcontract_amount")
    private Long subcontractAmount;

    @Column(name = "subcontract_type", length = 100)
    private String subcontractType;

    @Column(name = "purchase_order_no", length = 100)
    private String purchaseOrderNo;

    // 감사 필드
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 관계
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractTarget> targets = new ArrayList<>();

    @OneToMany(mappedBy = "contract")
    private List<Document> documents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.contractType == null) this.contractType = "UNIT_PRICE";
        if (this.progressStatus == null) this.progressStatus = "BUDGET";
        if (this.contractAmount == null) this.contractAmount = 0L;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
