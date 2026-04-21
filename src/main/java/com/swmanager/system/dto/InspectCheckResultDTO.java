package com.swmanager.system.dto;

import com.swmanager.system.domain.InspectCheckResult;
import lombok.Getter;
import lombok.Setter;

/**
 * S1 inspect-comprehensive-redesign: result → result_code + result_text 분리.
 * 하위 호환을 위해 getResult()/setResult() 편의 메서드는 result_text 로 위임.
 */
@Getter
@Setter
public class InspectCheckResultDTO {

    private Long id;
    private Long reportId;
    private String section;
    private String category;
    private String itemName;
    private String itemMethod;
    private String resultCode;
    private String resultText;
    private String remarks;
    private Integer sortOrder;

    /** 하위 호환 — UI 입력(자유 텍스트)은 resultText 로 매핑 */
    public String getResult() { return this.resultText; }
    public void setResult(String result) { this.resultText = result; }

    public static InspectCheckResultDTO fromEntity(InspectCheckResult e) {
        InspectCheckResultDTO dto = new InspectCheckResultDTO();
        dto.setId(e.getId());
        dto.setReportId(e.getReportId());
        dto.setSection(e.getSection());
        dto.setCategory(e.getCategory());
        dto.setItemName(e.getItemName());
        dto.setItemMethod(e.getItemMethod());
        dto.setResultCode(e.getResultCode());
        dto.setResultText(e.getResultText());
        dto.setRemarks(e.getRemarks());
        dto.setSortOrder(e.getSortOrder());
        return dto;
    }

    public InspectCheckResult toEntity(Long reportId) {
        InspectCheckResult e = new InspectCheckResult();
        e.setId(this.id);
        e.setReportId(reportId);
        e.setSection(this.section);
        e.setCategory(this.category);
        e.setItemName(this.itemName);
        e.setItemMethod(this.itemMethod);
        e.setResultCode(this.resultCode);
        e.setResultText(this.resultText);
        e.setRemarks(this.remarks);
        e.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);
        return e;
    }
}
