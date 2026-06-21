package com.swmanager.system.quotation.dto;

/**
 * /api/quotation/search (견적서 목록 검색) 응답 행 dto.
 *
 * 기존 비타입 응답조립(LinkedHashMap)을 타입 record 로 대체한다(§6-4 quotation-list-dto).
 * 소비자(quotation-form.html JS)는 JSON 키로 접근하므로 키·값·null 포함·키순서를 무손실 보존한다.
 *
 * <p><b>무손실 설계</b>:
 * <ul>
 *   <li>camelCase 키 = 컴포넌트명(전역 PropertyNamingStrategy 미설정) → 무어노테이션.</li>
 *   <li>{@code @JsonInclude} 미부착 → 현행이 8키를 null 값이어도 항상 담던 동작 동치 보존
 *       (전역 jackson default-property-inclusion 미설정 = null 포함).</li>
 *   <li>컴포넌트 선언순 = 현행 put 순(quoteId…createdBy)으로 키순서 보존(@JsonProperty 없어 Jackson 재정렬 없음).</li>
 *   <li>{@code quoteDate} 는 현행과 동일하게 {@code LocalDate.toString()}(yyyy-MM-dd), null 이면 빈 문자열.
 *       JS 가 {@code q.quoteDate.substring(5)} 로 접근하므로 String·non-null 보장.</li>
 * </ul>
 */
public record QuotationSearchRow(
        Long quoteId,
        String quoteNumber,
        String quoteDate,
        String category,
        String projectName,
        String recipient,
        Long grandTotal,
        String createdBy) {

    /** {@link QuotationDTO} → 검색 응답 행. 현행 응답조립과 1:1 동치(quoteDate 폴백 포함). */
    public static QuotationSearchRow from(QuotationDTO q) {
        return new QuotationSearchRow(
                q.getQuoteId(),
                q.getQuoteNumber(),
                q.getQuoteDate() != null ? q.getQuoteDate().toString() : "",
                q.getCategory(),
                q.getProjectName(),
                q.getRecipient(),
                q.getGrandTotal(),
                q.getCreatedBy());
    }
}
