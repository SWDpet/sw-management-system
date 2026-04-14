package com.swmanager.system.dto;

import com.swmanager.system.domain.InspectReport;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InspectReportDTO {

    private Long id;
    private Long pjtId;
    private String inspectMonth;
    private String sysType;
    private String docTitle;
    private String inspCompany;
    private String inspName;
    private String confOrg;
    private String confName;
    private String inspDbms;
    private String inspGis;
    private String dbmsIp;
    private String status;
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
     * - 같은 프로젝트의 이전 월 COMPLETED 보고서의 방문이력을 동적으로 조회
     * - 작성/미리보기/상세/PDF 화면에서만 표시
     */
    private List<InspectVisitLogDTO> previousVisits = new ArrayList<>();

    public static InspectReportDTO fromEntity(InspectReport e) {
        InspectReportDTO dto = new InspectReportDTO();
        dto.setId(e.getId());
        dto.setPjtId(e.getPjtId());
        dto.setInspectMonth(e.getInspectMonth());
        dto.setSysType(e.getSysType());
        dto.setDocTitle(e.getDocTitle());
        dto.setInspCompany(e.getInspCompany());
        dto.setInspName(e.getInspName());
        dto.setConfOrg(e.getConfOrg());
        dto.setConfName(e.getConfName());
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
        e.setInspCompany(this.inspCompany);
        e.setInspName(this.inspName);
        e.setConfOrg(this.confOrg);
        e.setConfName(this.confName);
        e.setInspDbms(this.inspDbms);
        e.setInspGis(this.inspGis);
        e.setDbmsIp(this.dbmsIp);
        e.setStatus(this.status != null ? this.status : "DRAFT");
        e.setInspSign(this.inspSign);
        e.setConfSign(this.confSign);
        return e;
    }
}
