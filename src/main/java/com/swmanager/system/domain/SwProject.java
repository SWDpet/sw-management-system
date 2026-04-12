package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sw_pjt")
public class SwProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proj_id")
    private Long projId;

    private Integer year;

    @Column(name = "pms_cd") private String pmsCd;
    @Column(name = "city_nm") private String cityNm;
    @Column(name = "dist_nm") private String distNm;
    @Column(name = "org_nm") private String orgNm;
    @Column(name = "org_cd") private String orgCd;
    @Column(name = "dist_cd") private String distCd;
    @Column(name = "biz_type") private String bizType;
    @Column(name = "biz_cat") private String bizCat;
    @Column(name = "biz_cat_en") private String bizCatEn;
    @Column(name = "sys_nm") private String sysNm;
    @Column(name = "sys_nm_en") private String sysNmEn;
    @Column(name = "proj_nm") private String projNm;

    private String client;

    @Column(name = "cont_ent") private String contEnt;
    @Column(name = "cont_dept") private String contDept;
    @Column(name = "cont_type") private String contType;

    @Column(name = "cont_dt")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate contDt;

    @Column(name = "start_dt")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDt;

    @Column(name = "end_dt")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDt;

    @Column(name = "inst_dt")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate instDt;

    @Column(name = "budg_amt") private Long budgAmt;
    @Column(name = "sw_rt") private Double swRt;
    @Column(name = "cont_amt") private Long contAmt;
    @Column(name = "cont_rt") private Double contRt;

    @Column(name = "sw_amt") private Long swAmt;
    @Column(name = "cnslt_amt") private Long cnsltAmt;
    @Column(name = "db_impl_amt") private Long dbImplAmt;
    @Column(name = "pkg_sw_amt") private Long pkgSwAmt;
    @Column(name = "sys_dev_amt") private Long sysDevAmt;
    @Column(name = "hw_amt") private Long hwAmt;
    @Column(name = "etc_amt") private Long etcAmt;
    @Column(name = "outscr_amt") private Long outscrAmt;

    @Column(name = "pay_prog_yn") private String payProgYn;
    @Column(name = "pay_prog_fr") private String payProgFr;
    @Column(name = "comp_yn") private String compYn;

    @Column(name = "interim_yn") private String interimYn;       // 기성계 대상 여부
    @Column(name = "completion_yn") private String completionYn; // 준공계 대상 여부

    // === 사업수행계획서 (P12) 필드 ===
    @Column(name = "proj_purpose", columnDefinition = "text")
    private String projPurpose;          // 용역의 목적

    @Column(name = "support_type", length = 50)
    private String supportType;          // 지원형태(상주/비상주)

    @Column(name = "scope_text", columnDefinition = "text")
    private String scopeText;            // 용역 범위(줄바꿈 구분)

    @Column(name = "inspect_method", length = 200)
    private String inspectMethod;        // 점검방법

    private String stat;

    @Column(name = "pre_pay") private Long prePay;
    @Column(name = "pay_prog") private Long payProg;
    @Column(name = "pay_comp") private Long payComp;

    @Column(name = "maint_type") private String maintType;

    @Column(name = "reg_dt") private LocalDateTime regDt;
    @Column(name = "person_id") private Long personId;
    @Column(name = "org_lgh_nm") private String orgLghNm;

    @PrePersist
    public void prePersist() {
        if (this.regDt == null) {
            this.regDt = LocalDateTime.now();
        }
    }
}
