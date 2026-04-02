package com.swmanager.system.dto;

import com.swmanager.system.domain.workplan.Contract;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사업현황(계약) DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {

    private Integer contractId;
    private Long infraId;

    // 인프라 정보 (표시용)
    private String cityNm;
    private String distNm;
    private String sysNm;

    // 계약 정보
    private String contractName;
    private String contractNo;
    private String contractType;       // UNIT_PRICE, PROCUREMENT, PRIVATE, SUBCONTRACT
    private String contractMethod;
    private String contractLaw;
    private String contractClause;
    private Long contractAmount;
    private String contractAmountText; // 한글 금액 (표시용)
    private Long guaranteeAmount;
    private BigDecimal guaranteeRate;
    private String contractDate;       // yyyy-MM-dd
    private String startDate;
    private String endDate;
    private String actualEndDate;
    private String progressStatus;     // BUDGET, CONTRACTING, COMMENCED, IN_PROGRESS, INTERIM, COMPLETED, SETTLED
    private Integer contractYear;

    // 발주처 정보
    private String clientOrg;
    private String clientAddr;
    private String clientPhone;
    private String clientFax;
    private String clientDept;
    private String clientContact;
    private String clientContactPhone;

    // 하도급 정보
    private String primeContractor;
    private Long subcontractAmount;
    private String subcontractType;
    private String purchaseOrderNo;

    // 관계 데이터
    private List<ParticipantDTO> participants;
    private List<TargetDTO> targets;

    /**
     * 과업참여자 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDTO {
        private Integer participantId;
        private Long userId;
        private String username;
        private String position;
        private String roleType;     // RESPONSIBLE, PARTICIPANT
        private String techGrade;    // 고급, 중급, 초급
        private String taskDesc;
        private Boolean isSiteRep;
        private Integer sortOrder;
    }

    /**
     * 유지보수 대상 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetDTO {
        private Integer targetId;
        private String targetCategory;  // S/W, H/W
        private String productName;
        private String productDetail;
        private Integer quantity;
        private Long swId;             // 인프라 SW 자동연동용
        private String swName;         // 표시용
        private Integer sortOrder;
    }

    /**
     * Entity → DTO 변환
     */
    public static ContractDTO fromEntity(Contract entity) {
        if (entity == null) return null;

        ContractDTOBuilder builder = ContractDTO.builder()
                .contractId(entity.getContractId())
                .infraId(entity.getInfra() != null ? entity.getInfra().getInfraId() : null)
                .cityNm(entity.getInfra() != null ? entity.getInfra().getCityNm() : null)
                .distNm(entity.getInfra() != null ? entity.getInfra().getDistNm() : null)
                .sysNm(entity.getInfra() != null ? entity.getInfra().getSysNm() : null)
                .contractName(entity.getContractName())
                .contractNo(entity.getContractNo())
                .contractType(entity.getContractType())
                .contractMethod(entity.getContractMethod())
                .contractLaw(entity.getContractLaw())
                .contractClause(entity.getContractClause())
                .contractAmount(entity.getContractAmount())
                .contractAmountText(convertToKoreanAmount(entity.getContractAmount()))
                .guaranteeAmount(entity.getGuaranteeAmount())
                .guaranteeRate(entity.getGuaranteeRate())
                .contractDate(entity.getContractDate() != null ? entity.getContractDate().toString() : null)
                .startDate(entity.getStartDate() != null ? entity.getStartDate().toString() : null)
                .endDate(entity.getEndDate() != null ? entity.getEndDate().toString() : null)
                .actualEndDate(entity.getActualEndDate() != null ? entity.getActualEndDate().toString() : null)
                .progressStatus(entity.getProgressStatus())
                .contractYear(entity.getContractYear())
                .clientOrg(entity.getClientOrg())
                .clientAddr(entity.getClientAddr())
                .clientPhone(entity.getClientPhone())
                .clientFax(entity.getClientFax())
                .clientDept(entity.getClientDept())
                .clientContact(entity.getClientContact())
                .clientContactPhone(entity.getClientContactPhone())
                .primeContractor(entity.getPrimeContractor())
                .subcontractAmount(entity.getSubcontractAmount())
                .subcontractType(entity.getSubcontractType())
                .purchaseOrderNo(entity.getPurchaseOrderNo());

        // 과업참여자
        if (entity.getParticipants() != null) {
            builder.participants(entity.getParticipants().stream()
                    .map(p -> ParticipantDTO.builder()
                            .participantId(p.getParticipantId())
                            .userId(p.getUser() != null ? p.getUser().getUserSeq() : null)
                            .username(p.getUser() != null ? p.getUser().getUsername() : null)
                            .position(p.getUser() != null ? p.getUser().getPosition() : null)
                            .roleType(p.getRoleType())
                            .techGrade(p.getTechGrade())
                            .taskDesc(p.getTaskDesc())
                            .isSiteRep(p.getIsSiteRep())
                            .sortOrder(p.getSortOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        // 유지보수 대상
        if (entity.getTargets() != null) {
            builder.targets(entity.getTargets().stream()
                    .map(t -> TargetDTO.builder()
                            .targetId(t.getTargetId())
                            .targetCategory(t.getTargetCategory())
                            .productName(t.getProductName())
                            .productDetail(t.getProductDetail())
                            .quantity(t.getQuantity())
                            .swId(t.getSoftware() != null ? t.getSoftware().getSwId() : null)
                            .swName(t.getSoftware() != null ? t.getSoftware().getSwNm() : null)
                            .sortOrder(t.getSortOrder())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    /**
     * 금액을 한글 표기로 변환
     */
    public static String convertToKoreanAmount(Long amount) {
        if (amount == null || amount == 0) return "";

        String[] units = {"", "만", "억", "조"};
        StringBuilder sb = new StringBuilder();
        long remaining = amount;
        int unitIdx = 0;

        while (remaining > 0) {
            long part = remaining % 10000;
            if (part > 0) {
                sb.insert(0, formatPart(part) + units[unitIdx] + " ");
            }
            remaining /= 10000;
            unitIdx++;
        }

        return "금 " + sb.toString().trim() + "원정";
    }

    private static String formatPart(long part) {
        String[] digits = {"", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};
        String[] subUnits = {"", "십", "백", "천"};
        StringBuilder sb = new StringBuilder();
        int pos = 0;

        while (part > 0) {
            int d = (int)(part % 10);
            if (d > 0) {
                String digitStr = (d == 1 && pos > 0) ? "" : digits[d];
                sb.insert(0, digitStr + subUnits[pos]);
            }
            part /= 10;
            pos++;
        }
        return sb.toString();
    }

    /**
     * 진행상태 한글명
     */
    public static String getStatusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "BUDGET": return "예산제안";
            case "CONTRACTING": return "계약진행";
            case "COMMENCED": return "착수";
            case "IN_PROGRESS": return "수행중";
            case "INTERIM": return "기성";
            case "COMPLETED": return "준공";
            case "SETTLED": return "정산완료";
            default: return status;
        }
    }

    /**
     * 계약유형 한글명
     */
    public static String getTypeLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "UNIT_PRICE": return "단가계약";
            case "PROCUREMENT": return "조달계약";
            case "PRIVATE": return "수의계약";
            case "SUBCONTRACT": return "하도급계약";
            default: return type;
        }
    }
}
