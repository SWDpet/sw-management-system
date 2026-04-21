package com.swmanager.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.domain.InspectReport;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * S1 inspect-comprehensive-redesign (A4):
 *  - 점검자: inspUserId + 조인 표시용 inspectorUsername/inspectorOrg
 *  - 확인자: confPsInfoId + 조인 표시용 confirmerName/confirmerOrg
 *  - 기존 inspCompany/inspName/confOrg/confName 문자열 4필드는 제거.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)  // S1: 기존 UI 의 inspName/confName 등 legacy field 무시
public class InspectReportDTO {

    private Long id;
    private Long pjtId;
    private String inspectMonth;
    private String sysType;
    private String docTitle;

    // S1 A4: FK 기반 점검자/확인자
    private Long inspUserId;
    private Long confPsInfoId;

    // 렌더링용 (조인 결과, DB 저장 X)
    private String inspectorUsername;
    private String inspectorOrg;
    private String confirmerName;
    private String confirmerOrg;

    private String inspDbms;
    private String inspGis;
    private String dbmsIp;
    private DocumentStatus status;
    private String inspSign;
    private String confSign;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<InspectVisitLogDTO> visits = new ArrayList<>();
    private List<InspectCheckResultDTO> checkResults = new ArrayList<>();

    /**
     * 이전 월 이력 (읽기전용, DB에 저장되지 않음)
     */
    private List<InspectVisitLogDTO> previousVisits = new ArrayList<>();

    public static InspectReportDTO fromEntity(InspectReport e) {
        InspectReportDTO dto = new InspectReportDTO();
        dto.setId(e.getId());
        dto.setPjtId(e.getPjtId());
        dto.setInspectMonth(e.getInspectMonth());
        dto.setSysType(e.getSysType());
        dto.setDocTitle(e.getDocTitle());
        dto.setInspUserId(e.getInspUserId());
        dto.setConfPsInfoId(e.getConfPsInfoId());
        dto.setInspDbms(e.getInspDbms());
        dto.setInspGis(e.getInspGis());
        dto.setDbmsIp(e.getDbmsIp());
        dto.setStatus(e.getStatus());
        dto.setInspSign(e.getInspSign());
        dto.setConfSign(e.getConfSign());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    public InspectReport toEntity() {
        InspectReport e = new InspectReport();
        e.setId(this.id);
        e.setPjtId(this.pjtId);
        e.setInspectMonth(this.inspectMonth);
        e.setSysType(this.sysType);
        e.setDocTitle(this.docTitle);
        e.setInspUserId(this.inspUserId);
        e.setConfPsInfoId(this.confPsInfoId);
        e.setInspDbms(this.inspDbms);
        e.setInspGis(this.inspGis);
        e.setDbmsIp(this.dbmsIp);
        e.setStatus(this.status != null ? this.status : DocumentStatus.DRAFT);
        e.setInspSign(this.inspSign);
        e.setConfSign(this.confSign);
        return e;
    }
}
