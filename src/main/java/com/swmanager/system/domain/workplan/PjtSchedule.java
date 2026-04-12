package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 예정공정표 (P2) - 작업공정 다중 행 + 월별 ON/OFF
 */
@Entity
@Table(name = "tb_pjt_schedule")
@Getter
@Setter
public class PjtSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proj_id", nullable = false)
    private Long projId;

    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;

    @Column(name = "m01") private Boolean m01 = false;
    @Column(name = "m02") private Boolean m02 = false;
    @Column(name = "m03") private Boolean m03 = false;
    @Column(name = "m04") private Boolean m04 = false;
    @Column(name = "m05") private Boolean m05 = false;
    @Column(name = "m06") private Boolean m06 = false;
    @Column(name = "m07") private Boolean m07 = false;
    @Column(name = "m08") private Boolean m08 = false;
    @Column(name = "m09") private Boolean m09 = false;
    @Column(name = "m10") private Boolean m10 = false;
    @Column(name = "m11") private Boolean m11 = false;
    @Column(name = "m12") private Boolean m12 = false;

    @Column(name = "remark", length = 300)
    private String remark;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
