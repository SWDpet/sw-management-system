package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 사업별 과업참여자 배정 (현장대리인 포함)
 * 테이블: tb_contract_participant
 */
@Entity
@Table(name = "tb_contract_participant")
@Getter
@Setter
public class ContractParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Integer participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proj_id")
    private SwProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** 역할: RESPONSIBLE(책임기술자), PARTICIPANT(참여기술자) */
    @Column(name = "role_type", length = 30)
    private String roleType;

    /** 기술등급 (고급/중급/초급 등) */
    @Column(name = "tech_grade", length = 30)
    private String techGrade;

    /** 과업참여 업무 */
    @Column(name = "task_desc", length = 500)
    private String taskDesc;

    /** 현장대리인 여부 */
    @Column(name = "is_site_rep")
    private Boolean isSiteRep = false;

    /** 정렬순서 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
