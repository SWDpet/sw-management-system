package com.swmanager.system.dto;

import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    private Integer docId;
    private String docNo;
    private String docType;
    private String sysType;
    private Long infraId;
    private String cityNm;
    private String distNm;
    private String sysNm;
    private Integer planId;
    private Integer contractId;
    private String contractName;
    private Long projId;
    private String projNm;
    private String projClient;
    private Long projContAmt;
    private String projStartDt;
    private String projEndDt;
    private String title;
    private String status;
    private Long authorId;
    private String authorName;
    private Long approverId;
    private String approverName;
    private String approvedAt;
    private String createdAt;
    private String updatedAt;

    // 문서 상세 섹션
    private List<DetailSectionDTO> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailSectionDTO {
        private Integer detailId;
        private String sectionKey;
        private Map<String, Object> sectionData;
        private Integer sortOrder;

        public static DetailSectionDTO fromEntity(DocumentDetail detail) {
            return DetailSectionDTO.builder()
                    .detailId(detail.getDetailId())
                    .sectionKey(detail.getSectionKey())
                    .sectionData(detail.getSectionData())
                    .sortOrder(detail.getSortOrder())
                    .build();
        }
    }

    public static DocumentDTO fromEntity(Document doc) {
        DocumentDTOBuilder builder = DocumentDTO.builder()
                .docId(doc.getDocId())
                .docNo(doc.getDocNo())
                .docType(doc.getDocType())
                .sysType(doc.getSysType())
                .title(doc.getTitle())
                .status(doc.getStatus())
                .createdAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null)
                .updatedAt(doc.getUpdatedAt() != null ? doc.getUpdatedAt().toString() : null);

        if (doc.getInfra() != null) {
            builder.infraId(doc.getInfra().getInfraId())
                   .cityNm(doc.getInfra().getCityNm())
                   .distNm(doc.getInfra().getDistNm())
                   .sysNm(doc.getInfra().getSysNm());
        }
        if (doc.getWorkPlan() != null) {
            builder.planId(doc.getWorkPlan().getPlanId());
        }
        if (doc.getProject() != null) {
            builder.projId(doc.getProject().getProjId())
                   .projNm(doc.getProject().getProjNm())
                   .projClient(doc.getProject().getClient())
                   .projContAmt(doc.getProject().getContAmt())
                   .projStartDt(doc.getProject().getStartDt() != null ? doc.getProject().getStartDt().toString() : null)
                   .projEndDt(doc.getProject().getEndDt() != null ? doc.getProject().getEndDt().toString() : null);

            // infra가 없는 경우 project에서 cityNm/distNm/sysNm fallback
            if (doc.getInfra() == null) {
                builder.cityNm(doc.getProject().getCityNm())
                       .distNm(doc.getProject().getDistNm())
                       .sysNm(doc.getProject().getSysNm());
            }
        }
        if (doc.getAuthor() != null) {
            builder.authorId(doc.getAuthor().getUserSeq())
                   .authorName(doc.getAuthor().getUsername());
        }
        if (doc.getApprover() != null) {
            builder.approverId(doc.getApprover().getUserSeq())
                   .approverName(doc.getApprover().getUsername());
        }
        if (doc.getApprovedAt() != null) {
            builder.approvedAt(doc.getApprovedAt().toString());
        }

        // 상세 섹션
        if (doc.getDetails() != null && !doc.getDetails().isEmpty()) {
            builder.sections(doc.getDetails().stream()
                    .map(DetailSectionDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    // 유틸리티: 문서유형 한글
    public static String getDocTypeLabel(String docType) {
        if (docType == null) return "";
        return switch (docType) {
            case "COMMENCE" -> "착수계";
            case "INTERIM" -> "기성계";
            case "COMPLETION" -> "준공계";
            case "INSPECT" -> "점검내역서";
            case "FAULT" -> "장애처리";
            case "SUPPORT" -> "업무지원";
            case "INSTALL" -> "설치보고서";
            case "PATCH" -> "패치내역서";
            default -> docType;
        };
    }

    public static String getStatusLabel(String status) {
        if (status == null) return "";
        return switch (status) {
            case "DRAFT" -> "작성중";
            case "COMPLETED" -> "작성완료";
            default -> status;
        };
    }

    public static String getStatusColor(String status) {
        if (status == null) return "#858796";
        return switch (status) {
            case "DRAFT" -> "#858796";
            case "COMPLETED" -> "#1cc88a";
            default -> "#858796";
        };
    }
}
