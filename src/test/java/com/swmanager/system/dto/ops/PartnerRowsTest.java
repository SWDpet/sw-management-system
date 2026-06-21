package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PartnerListRow / PartnerListContactRow 직렬화 골든 테스트 (partner-list-rows-dto §6-4).
 *
 * 기존 PartnerController.list(/api/list) 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 바꾸면서,
 * 응답 JSON 이 무손실(snake_case 키셋·값·null·중첩 contacts)인지 고정한다.
 * 현행 LinkedHashMap 이라 키순서 결정적이나 클라이언트 키접근 → JsonNode tree 동치만 검증.
 */
class PartnerRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Object legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    /** 현행 컨트롤러 로직 복제(검증 기준, snake_case 키). */
    private Map<String, Object> legacy(Partner p, List<PartnerContact> contacts) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("partner_id", p.getPartnerId());
        m.put("name", p.getName());
        m.put("partner_type", p.getPartnerType());
        m.put("main_tel", p.getMainTel());
        m.put("note", p.getNote());
        List<Map<String, Object>> cs = new ArrayList<>();
        for (PartnerContact c : contacts) {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("contact_id", c.getContactId());
            cm.put("name", c.getName());
            cm.put("position", c.getPosition());
            cm.put("tel", c.getTel());
            cm.put("email", c.getEmail());
            cs.add(cm);
        }
        m.put("contacts", cs);
        return m;
    }

    private PartnerListRow toRow(Partner p, List<PartnerContact> contacts) {
        List<PartnerListContactRow> rows = contacts.stream().map(PartnerListContactRow::from).toList();
        return PartnerListRow.from(p, rows);
    }

    @Test
    void partnerRow_full_matchesLegacy_snakeKeys() {
        Partner p = new Partner();
        p.setPartnerId(3L);
        p.setName("정도유아이티");
        p.setPartnerType("유지보수");
        p.setMainTel("02-1234-5678");
        p.setNote("주 협력사");
        PartnerContact c = new PartnerContact();
        c.setContactId(7L);
        c.setName("김담당");
        c.setPosition("팀장");
        c.setTel("010-1111-2222");
        c.setEmail("kim@example.com");
        List<PartnerContact> contacts = List.of(c);

        assertTreeEqual(toRow(p, contacts), legacy(p, contacts));

        // snake_case 키명 실증
        JsonNode j = om.valueToTree(toRow(p, contacts));
        assertThat(j.has("partner_id")).isTrue();
        assertThat(j.has("partner_type")).isTrue();
        assertThat(j.has("main_tel")).isTrue();
        assertThat(j.has("partnerId")).isFalse();
        assertThat(j.get("contacts").get(0).has("contact_id")).isTrue();
        assertThat(j.get("contacts").get(0).has("contactId")).isFalse();
    }

    @Test
    void partnerRow_nulls_and_emptyContacts() {
        Partner p = new Partner();
        p.setPartnerId(4L);     // name/type/tel/note null
        List<PartnerContact> none = List.of();

        assertTreeEqual(toRow(p, none), legacy(p, none));

        JsonNode j = om.valueToTree(toRow(p, none));
        assertThat(j.get("name").isNull()).isTrue();       // null 키 보존
        assertThat(j.get("main_tel").isNull()).isTrue();
        assertThat(j.get("contacts").isArray()).isTrue();
        assertThat(j.get("contacts").size()).isZero();
    }
}
