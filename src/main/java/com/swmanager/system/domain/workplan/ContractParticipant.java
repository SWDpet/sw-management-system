package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_contract_participant")
@Getter @Setter
public class ContractParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Integer participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "role_type", nullable = false, length = 20)
    private String roleType; // RESPONSIBLE, PARTICIPANT

    @Column(name = "tech_grade", length = 20)
    private String techGrade; // 고급, 중급, 초급

    @Column(name = "task_desc", length = 500)
    private String taskDesc;

    @Column(name = "is_site_rep", nullable = false)
    private Boolean isSiteRep = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @PrePersist
    public void prePersist() {
        if (this.roleType == null) this.roleType = "PARTICIPANT";
        if (this.isSiteRep == null) this.isSiteRep = false;
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
