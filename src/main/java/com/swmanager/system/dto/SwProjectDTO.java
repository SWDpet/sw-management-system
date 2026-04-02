package com.swmanager.system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SW 프로젝트 데이터 전송 객체
 * Entity를 직접 노출하지 않고 필요한 데이터만 전송
 */
@Data
@Builder
@NoArgsConstructor  // ← 기본 생성자 추가!
@AllArgsConstructor // ← 모든 필드 생성자 추가!
public class SwProjectDTO {
    
    // 기본 정보
    private Long projId;
    private Integer year;
    private String pmsCd;
    
    // 지역 정보
    private String cityNm;
    private String distNm;
    private String orgNm;
    private String orgCd;
    private String distCd;
    
    // 사업 정보
    private String bizType;
    private String bizCat;
    private String bizCatEn;
    private String sysNm;
    private String sysNmEn;
    private String projNm;
    private String client;
    
    // 계약 정보
    private String contEnt;
    private String contDept;
    private String contType;
    private LocalDate contDt;
    private LocalDate startDt;
    private LocalDate endDt;
    private LocalDate instDt;
    
    // 금액 정보 (민감하지 않은 정보만 노출)
    private Long budgAmt;
    private Double swRt;
    private Long contAmt;
    private Double contRt;
    
    // 상세 금액 (권한에 따라 선택적 노출)
    private Long swAmt;
    private Long cnsltAmt;
    private Long dbImplAmt;
    private Long pkgSwAmt;
    private Long sysDevAmt;
    private Long hwAmt;
    private Long etcAmt;
    private Long outscrAmt;
    
    // 진행 상태
    private String stat;
    private String statNm; // 상태명 (추가)
    private String payProgYn;
    private String payProgFr;
    private Long prePay;
    private Long payProg;
    private Long payComp;
    
    // 유지보수
    private String maintType;
    private String maintTypeNm; // 유지보수 유형명 (추가)
    
    // 메타 정보
    private LocalDateTime regDt;
    private Long personId;
    private String personNm; // 담당자명 (조인 데이터)
    private String orgLghNm;
    
    // Entity에서 DTO로 변환하는 정적 메서드
    public static SwProjectDTO fromEntity(com.swmanager.system.domain.SwProject entity) {
        if (entity == null) {
            return null;
        }
        
        return SwProjectDTO.builder()
                .projId(entity.getProjId())
                .year(entity.getYear())
                .pmsCd(entity.getPmsCd())
                .cityNm(entity.getCityNm())
                .distNm(entity.getDistNm())
                .orgNm(entity.getOrgNm())
                .orgCd(entity.getOrgCd())
                .distCd(entity.getDistCd())
                .bizType(entity.getBizType())
                .bizCat(entity.getBizCat())
                .bizCatEn(entity.getBizCatEn())
                .sysNm(entity.getSysNm())
                .sysNmEn(entity.getSysNmEn())
                .projNm(entity.getProjNm())
                .client(entity.getClient())
                .contEnt(entity.getContEnt())
                .contDept(entity.getContDept())
                .contType(entity.getContType())
                .contDt(entity.getContDt())
                .startDt(entity.getStartDt())
                .endDt(entity.getEndDt())
                .instDt(entity.getInstDt())
                .budgAmt(entity.getBudgAmt())
                .swRt(entity.getSwRt())
                .contAmt(entity.getContAmt())
                .contRt(entity.getContRt())
                .swAmt(entity.getSwAmt())
                .cnsltAmt(entity.getCnsltAmt())
                .dbImplAmt(entity.getDbImplAmt())
                .pkgSwAmt(entity.getPkgSwAmt())
                .sysDevAmt(entity.getSysDevAmt())
                .hwAmt(entity.getHwAmt())
                .etcAmt(entity.getEtcAmt())
                .outscrAmt(entity.getOutscrAmt())
                .stat(entity.getStat())
                .payProgYn(entity.getPayProgYn())
                .payProgFr(entity.getPayProgFr())
                .prePay(entity.getPrePay())
                .payProg(entity.getPayProg())
                .payComp(entity.getPayComp())
                .maintType(entity.getMaintType())
                .regDt(entity.getRegDt())
                .personId(entity.getPersonId())
                .orgLghNm(entity.getOrgLghNm())
                .build();
    }
    
    // DTO에서 Entity로 변환 (저장 시 사용)
    public com.swmanager.system.domain.SwProject toEntity() {
        com.swmanager.system.domain.SwProject entity = new com.swmanager.system.domain.SwProject();
        
        entity.setProjId(this.projId);
        entity.setYear(this.year);
        entity.setPmsCd(this.pmsCd);
        entity.setCityNm(this.cityNm);
        entity.setDistNm(this.distNm);
        entity.setOrgNm(this.orgNm);
        entity.setOrgCd(this.orgCd);
        entity.setDistCd(this.distCd);
        entity.setBizType(this.bizType);
        entity.setBizCat(this.bizCat);
        entity.setBizCatEn(this.bizCatEn);
        entity.setSysNm(this.sysNm);
        entity.setSysNmEn(this.sysNmEn);
        entity.setProjNm(this.projNm);
        entity.setClient(this.client);
        entity.setContEnt(this.contEnt);
        entity.setContDept(this.contDept);
        entity.setContType(this.contType);
        entity.setContDt(this.contDt);
        entity.setStartDt(this.startDt);
        entity.setEndDt(this.endDt);
        entity.setInstDt(this.instDt);
        entity.setBudgAmt(this.budgAmt);
        entity.setSwRt(this.swRt);
        entity.setContAmt(this.contAmt);
        entity.setContRt(this.contRt);
        entity.setSwAmt(this.swAmt);
        entity.setCnsltAmt(this.cnsltAmt);
        entity.setDbImplAmt(this.dbImplAmt);
        entity.setPkgSwAmt(this.pkgSwAmt);
        entity.setSysDevAmt(this.sysDevAmt);
        entity.setHwAmt(this.hwAmt);
        entity.setEtcAmt(this.etcAmt);
        entity.setOutscrAmt(this.outscrAmt);
        entity.setStat(this.stat);
        entity.setPayProgYn(this.payProgYn);
        entity.setPayProgFr(this.payProgFr);
        entity.setPrePay(this.prePay);
        entity.setPayProg(this.payProg);
        entity.setPayComp(this.payComp);
        entity.setMaintType(this.maintType);
        entity.setPersonId(this.personId);
        entity.setOrgLghNm(this.orgLghNm);
        
        return entity;
    }
}
