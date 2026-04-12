package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 시스템별 공정명 마스터
 * 테이블: tb_process_master
 */
@Entity
@Table(name = "tb_process_master")
@Getter
@Setter
public class ProcessMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id")
    private Integer processId;

    /** 시스템 영문명 (UPIS, KRAS, IPSS 등) */
    @Column(name = "sys_nm_en", length = 30)
    private String sysNmEn;

    /** 공정명 (예: "도시계획정보체계용 GIS SW 유지관리") */
    @Column(name = "process_name", length = 200)
    private String processName;

    /** 정렬순서 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 사용여부 */
    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";
}
