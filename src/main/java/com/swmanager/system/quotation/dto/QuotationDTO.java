package com.swmanager.system.quotation.dto;

import com.swmanager.system.quotation.domain.Quotation;
import com.swmanager.system.quotation.domain.QuotationItem;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDTO {

    private Long quoteId;
    private String quoteNumber;
    private LocalDate quoteDate;
    private String category;
    private String projectName;
    private String recipient;
    private String referenceTo;
    private Long totalAmount;
    private String totalAmountText;
    private Long grandTotal;
    private Double bidRate;
    private Boolean vatIncluded;
    private Integer rounddownUnit;
    private Boolean showSeal;
    private Integer templateType;
    private String remarks;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<ItemDTO> items = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        private Long itemId;
        private Integer itemNo;
        private String productName;
        private String specification;
        private Integer quantity;
        private String unit;
        private Long unitPrice;
        private Long amount;
        private String remarks;

        public static ItemDTO fromEntity(QuotationItem entity) {
            return ItemDTO.builder()
                    .itemId(entity.getItemId())
                    .itemNo(entity.getItemNo())
                    .productName(entity.getProductName())
                    .specification(entity.getSpecification())
                    .quantity(entity.getQuantity())
                    .unit(entity.getUnit())
                    .unitPrice(entity.getUnitPrice())
                    .amount(entity.getAmount())
                    .remarks(entity.getRemarks())
                    .build();
        }

        public QuotationItem toEntity() {
            QuotationItem item = new QuotationItem();
            item.setItemId(this.itemId);
            item.setItemNo(this.itemNo);
            item.setProductName(this.productName);
            item.setSpecification(this.specification);
            item.setQuantity(this.quantity != null ? this.quantity : 1);
            item.setUnit(this.unit != null ? this.unit : "식");
            item.setUnitPrice(this.unitPrice != null ? this.unitPrice : 0L);
            item.setAmount(this.amount != null ? this.amount : 0L);
            item.setRemarks(this.remarks);
            return item;
        }
    }

    public static QuotationDTO fromEntity(Quotation entity) {
        QuotationDTOBuilder builder = QuotationDTO.builder()
                .quoteId(entity.getQuoteId())
                .quoteNumber(entity.getQuoteNumber())
                .quoteDate(entity.getQuoteDate())
                .category(entity.getCategory())
                .projectName(entity.getProjectName())
                .recipient(entity.getRecipient())
                .referenceTo(entity.getReferenceTo())
                .totalAmount(entity.getTotalAmount())
                .totalAmountText(entity.getTotalAmountText())
                .grandTotal(entity.getGrandTotal())
                .bidRate(entity.getBidRate())
                .vatIncluded(entity.getVatIncluded())
                .rounddownUnit(entity.getRounddownUnit())
                .showSeal(entity.getShowSeal())
                .templateType(entity.getTemplateType())
                .remarks(entity.getRemarks())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt());

        if (entity.getItems() != null) {
            builder.items(entity.getItems().stream()
                    .map(ItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public Quotation toEntity() {
        Quotation q = new Quotation();
        q.setQuoteId(this.quoteId);
        q.setQuoteDate(this.quoteDate != null ? this.quoteDate : LocalDate.now());
        q.setCategory(this.category);
        q.setProjectName(this.projectName);
        q.setRecipient(this.recipient);
        q.setReferenceTo(this.referenceTo);
        q.setTotalAmount(this.totalAmount);
        q.setTotalAmountText(this.totalAmountText);
        q.setGrandTotal(this.grandTotal);
        q.setBidRate(this.bidRate);
        q.setVatIncluded(this.vatIncluded != null ? this.vatIncluded : true);
        q.setRounddownUnit(this.rounddownUnit != null ? this.rounddownUnit : 1);
        q.setShowSeal(this.showSeal != null ? this.showSeal : true);
        q.setTemplateType(this.templateType != null ? this.templateType : 1);
        q.setRemarks(this.remarks);
        q.setStatus(this.status != null ? this.status : "작성중");
        q.setCreatedBy(this.createdBy);
        return q;
    }

    /** 금액을 한글로 변환 */
    public static String amountToKorean(long num) {
        if (num == 0) return "영원";
        String[] units = {"", "만", "억", "조"};
        String[] smallUnits = {"", "십", "백", "천"};
        String[] digits = {"", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};

        StringBuilder result = new StringBuilder();
        int groupIdx = 0;

        while (num > 0) {
            int group = (int)(num % 10000);
            if (group > 0) {
                StringBuilder groupStr = new StringBuilder();
                for (int i = 0; i < 4; i++) {
                    int d = group % 10;
                    if (d > 0) {
                        String digitStr = (i == 0) ? digits[d] : ((d > 1 ? digits[d] : "") + smallUnits[i]);
                        groupStr.insert(0, digitStr);
                    }
                    group /= 10;
                }
                result.insert(0, groupStr.toString() + units[groupIdx]);
            }
            groupIdx++;
            num /= 10000;
        }

        return "일금 " + result + "원 정";
    }
}
