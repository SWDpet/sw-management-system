package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * 사업수행계획서 - 공정별 투입인력계획 (P13)
 * 한 사업당 N개 작업단계, 각 단계마다 등급별 인원수
 */
@Entity
@Table(name = "tb_pjt_manpower_plan")
@Getter
@Setter
public class PjtManpowerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proj_id", nullable = false)
    private Long projId;

    @Column(name = "step_name", nullable = false, length = 200)
    private String stepName;

    @Column(name = "start_dt")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDt;

    @Column(name = "end_dt")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDt;

    @Column(name = "grade_special") private Integer gradeSpecial = 0; // 특급기술자
    @Column(name = "grade_high")    private Integer gradeHigh    = 0; // 고급기술자
    @Column(name = "grade_mid")     private Integer gradeMid     = 0; // 중급기술자
    @Column(name = "grade_low")     private Integer gradeLow     = 0; // 초급기술자
    @Column(name = "func_high")     private Integer funcHigh     = 0; // 고급기능사
    @Column(name = "func_mid")      private Integer funcMid      = 0; // 중급기능사
    @Column(name = "func_low")      private Integer funcLow      = 0; // 초급기능사

    @Column(name = "remark", length = 300)
    private String remark;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 합계 (DB 컬럼 아님 - 출력시 사용) */
    @Transient
    public int getTotal() {
        return safe(gradeSpecial) + safe(gradeHigh) + safe(gradeMid) + safe(gradeLow)
                + safe(funcHigh) + safe(funcMid) + safe(funcLow);
    }
    private int safe(Integer v) { return v == null ? 0 : v; }
}
