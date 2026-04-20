package com.swmanager.system.dto;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
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
    private DocumentType docType;
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
    private DocumentStatus status;
    private Long authorId;
    private String authorName;
    private Long approverId;
    private String approverName;
    private String approvedAt;
    private String createdAt;
    private String updatedAt;

    // [스프린트 5] 새 필드
    private String supportTargetType; // EXTERNAL / INTERNAL
    private Long orgUnitId;
    private String orgUnitName;
    private String orgUnitPath;       // "본부 > 부서 > 팀" 표시용
    private String environment;       // PROD / TEST
    private String targetDisplay;     // 목록 표시용 통합 텍스트 (FR-2-F)
    private String environmentLabel;  // 운영 / 테스트 (FR-4-C)

    // [v2] 4개 문서용 — region_code 기반 지역 표시 (sysNm 은 기존 필드 재사용)
    private String regionCode;        // sigungu_code.adm_sect_c
    private String regionSidoNm;      // 조회 시 sigungu_code 에서 룩업
    private String regionSigunguNm;   // 조회 시 sigungu_code 에서 룩업

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

        // [스프린트 5] 새 필드
        builder.supportTargetType(doc.getSupportTargetType())
               .environment(doc.getEnvironment())
               .environmentLabel(getEnvironmentLabel(doc.getEnvironment()))
               .regionCode(doc.getRegionCode());

        if (doc.getOrgUnit() != null) {
            builder.orgUnitId(doc.getOrgUnit().getUnitId())
                   .orgUnitName(doc.getOrgUnit().getName())
                   .orgUnitPath(buildOrgUnitPath(doc.getOrgUnit()));
        }

        // 목록 표시용 통합 타겟 텍스트 (FR-2-F)
        builder.targetDisplay(buildTargetDisplay(doc));

        return builder.build();
    }

    private static String getEnvironmentLabel(String env) {
        if (env == null) return null;
        return switch (env) {
            case "PROD" -> "운영";
            case "TEST" -> "테스트";
            default -> env;
        };
    }

    /** 조직 유닛의 루트→대상 이름 경로를 " > " 로 연결. 순환 방어 포함. */
    private static String buildOrgUnitPath(com.swmanager.system.domain.OrgUnit leaf) {
        if (leaf == null) return null;
        java.util.LinkedList<String> names = new java.util.LinkedList<>();
        var cur = leaf;
        int guard = 0;
        while (cur != null && guard++ < 10) {
            names.addFirst(cur.getName());
            cur = cur.getParent();
        }
        return String.join(" > ", names);
    }

    private static String buildTargetDisplay(Document doc) {
        if (DocumentType.SUPPORT == doc.getDocType() && "INTERNAL".equals(doc.getSupportTargetType())) {
            if (doc.getOrgUnit() != null) {
                return buildOrgUnitPath(doc.getOrgUnit());
            }
            return "내부";
        }
        // [v2] 4개 문서 region_code 기반 — 실제 시도/시군구명은 Service.enrichRegion 에서 보강됨
        // 여기서는 regionCode/sysType 존재 여부로만 판단. 최종 문자열은 Service 가 targetDisplay 재계산.
        // (fromEntity 시점엔 Repository 접근 불가이므로 placeholder 만 반환)
        if (doc.getRegionCode() != null) {
            return "[" + doc.getRegionCode() + "] " + (doc.getSysType() != null ? doc.getSysType() : "");
        }
        if (doc.getProject() != null) {
            var p = doc.getProject();
            StringBuilder sb = new StringBuilder();
            if (p.getCityNm() != null) sb.append(p.getCityNm()).append(' ');
            if (p.getDistNm() != null && !p.getDistNm().equals(p.getCityNm())) sb.append(p.getDistNm()).append(' ');
            if (p.getSysNm() != null) sb.append("- ").append(p.getSysNm());
            return sb.toString().trim();
        }
        if (doc.getInfra() != null) {
            var i = doc.getInfra();
            StringBuilder sb = new StringBuilder();
            if (i.getCityNm() != null) sb.append(i.getCityNm()).append(' ');
            if (i.getDistNm() != null && !i.getDistNm().equals(i.getCityNm())) sb.append(i.getDistNm());
            return sb.toString().trim();
        }
        return "";
    }

    // 유틸리티: 문서유형 한글
    public static String getDocTypeLabel(DocumentType docType) {
        return docType != null ? docType.label() : "-";
    }

    public static String getStatusLabel(DocumentStatus status) {
        return status != null ? status.label() : "-";
    }

    public static String getStatusColor(DocumentStatus status) {
        return status != null ? status.color() : "#858796";
    }
}
