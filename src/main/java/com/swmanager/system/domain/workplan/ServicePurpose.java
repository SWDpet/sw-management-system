package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 시스템별 용역 목적 / 과업 내용 마스터
 * 테이블: tb_service_purpose
 */
@Entity
@Table(name = "tb_service_purpose")
@Getter
@Setter
public class ServicePurpose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purpose_id")
    private Integer purposeId;

    /** 시스템 영문명 (UPIS, KRAS, IPSS 등) */
    @Column(name = "sys_nm_en", length = 30)
    private String sysNmEn;

    /** 구분: PURPOSE(용역목적), TASK(과업내용), SCOPE(용역범위) */
    @Column(name = "purpose_type", length = 20)
    private String purposeType;

    /** 내용 텍스트 */
    @Column(name = "purpose_text", columnDefinition = "TEXT")
    private String purposeText;

    /** 정렬순서 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 사용여부 */
    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";
}
