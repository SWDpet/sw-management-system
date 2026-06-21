package com.swmanager.system.quotation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.SwProject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QuotationSearchRow / SwClientRow 직렬화 골든 테스트 (quotation-list-dto §6-4).
 *
 * 기존 QuotationController 의 응답조립 {@code List<Map<String,Object>>}(LinkedHashMap)을 record 로
 * 바꾸면서, /api/quotation/search · /api/quotation/sw-projects/clients 응답 JSON 이 무손실
 * (키셋·값·키순서·null 포함·quoteDate 폴백)인지 고정한다. 소비자(quotation-form.html JS)는 camelCase 키로 접근.
 */
class QuotationListRowTest {

    private final ObjectMapper om = new ObjectMapper();

    // ===== 치환 전 응답조립을 그대로 복제(골든 기준) =====
    private Map<String, Object> legacySearch(QuotationDTO q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("quoteId", q.getQuoteId());
        m.put("quoteNumber", q.getQuoteNumber());
        m.put("quoteDate", q.getQuoteDate() != null ? q.getQuoteDate().toString() : "");
        m.put("category", q.getCategory());
        m.put("projectName", q.getProjectName());
        m.put("recipient", q.getRecipient());
        m.put("grandTotal", q.getGrandTotal());
        m.put("createdBy", q.getCreatedBy());
        return m;
    }

    private Map<String, Object> legacyClient(SwProject p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("projId", p.getProjId());
        m.put("client", p.getClient());
        m.put("swAmt", p.getSwAmt());
        m.put("projNm", p.getProjNm());
        m.put("year", p.getYear());
        m.put("distNm", p.getDistNm());
        return m;
    }

    private QuotationDTO fullQuotation() {
        return QuotationDTO.builder()
                .quoteId(7L)
                .quoteNumber("Q-2026-0007")
                .quoteDate(LocalDate.of(2026, 6, 21))
                .category("SW")
                .projectName("강진군 UPIS 유지관리")
                .recipient("강진군청")
                .grandTotal(12_300_000L)
                .createdBy("ukjin")
                .build();
    }

    private SwProject fullProject() {
        SwProject p = new SwProject();
        p.setProjId(101L);
        p.setClient("강진군청");
        p.setSwAmt(5_000_000L);
        p.setProjNm("UPIS 유지관리");
        p.setYear(2026);
        p.setDistNm("강진군");
        return p;
    }

    @Test
    void search_treeAndStringEqualLegacy() throws Exception {
        QuotationDTO q = fullQuotation();
        JsonNode dto = om.valueToTree(QuotationSearchRow.from(q));
        JsonNode legacy = om.valueToTree(legacySearch(q));
        assertThat(dto).isEqualTo(legacy);                                   // 키셋·값
        assertThat(om.writeValueAsString(QuotationSearchRow.from(q)))
                .isEqualTo(om.writeValueAsString(legacySearch(q)));          // 키순서
        assertThat(dto.get("quoteDate").asText()).isEqualTo("2026-06-21");
    }

    @Test
    void search_quoteDateNull_fallsBackToEmptyString() throws Exception {
        QuotationDTO q = fullQuotation();
        q.setQuoteDate(null);                       // null → "" (JS substring 안전)
        JsonNode dto = om.valueToTree(QuotationSearchRow.from(q));
        assertThat(dto.get("quoteDate").asText()).isEmpty();
        assertThat(dto.get("quoteDate").isNull()).isFalse();   // null 아님 = 빈 문자열
        JsonNode legacy = om.valueToTree(legacySearch(q));
        assertThat(dto).isEqualTo(legacy);
    }

    @Test
    void search_nullFields_includedAsNull() throws Exception {
        QuotationDTO q = QuotationDTO.builder().quoteId(1L).build();   // 대부분 null
        JsonNode dto = om.valueToTree(QuotationSearchRow.from(q));
        assertThat(dto.has("createdBy")).isTrue();
        assertThat(dto.get("createdBy").isNull()).isTrue();
        assertThat(dto.size()).isEqualTo(8);          // 8키 전부 존재
        JsonNode legacy = om.valueToTree(legacySearch(q));
        assertThat(dto).isEqualTo(legacy);
    }

    @Test
    void client_treeAndStringEqualLegacy() throws Exception {
        SwProject p = fullProject();
        JsonNode dto = om.valueToTree(SwClientRow.from(p));
        JsonNode legacy = om.valueToTree(legacyClient(p));
        assertThat(dto).isEqualTo(legacy);                                   // 키셋·값
        assertThat(om.writeValueAsString(SwClientRow.from(p)))
                .isEqualTo(om.writeValueAsString(legacyClient(p)));          // 키순서
    }

    @Test
    void client_nullFields_includedAsNull() throws Exception {
        SwProject p = new SwProject();
        p.setProjId(2L);                              // 나머지 null
        JsonNode dto = om.valueToTree(SwClientRow.from(p));
        assertThat(dto.has("distNm")).isTrue();
        assertThat(dto.get("distNm").isNull()).isTrue();
        assertThat(dto.size()).isEqualTo(6);          // 6키 전부 존재
        JsonNode legacy = om.valueToTree(legacyClient(p));
        assertThat(dto).isEqualTo(legacy);
    }
}
