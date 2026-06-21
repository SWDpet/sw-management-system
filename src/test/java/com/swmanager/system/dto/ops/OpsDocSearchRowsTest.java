package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocPartner;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import com.swmanager.system.domain.ops.Staff;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpsDocController 사람/요청자 검색 응답 record 6종 직렬화 골든 테스트 (opsdoc-search-rows-dto §6-4).
 *
 * 기존 컨트롤러-로컬 {@code HashMap} 응답조립을 record 로 바꾸면서, 응답 JSON 의 키셋·값·null 포함·폴백을
 * 무손실 보존하는지 고정한다. 현행이 HashMap 이라 키순서는 비결정 → JsonNode tree 동치(키셋·값)만 검증.
 */
class OpsDocSearchRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Map<String, Object> legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    // ===== engineers =====
    @Test
    void engineerRow_matchesLegacy_andPositionFallback() {
        User u = new User();
        u.setUserSeq(5L);
        u.setUsername("박욱진");
        u.setPositionTitle("대리");          // position=null → positionTitle 폴백
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getUserSeq());
        m.put("name", u.getUsername());
        m.put("position", u.getPosition() != null ? u.getPosition() : u.getPositionTitle());
        assertTreeEqual(EngineerRow.from(u), m);
        assertThat(om.valueToTree(EngineerRow.from(u)).get("position").asText()).isEqualTo("대리");

        u.setPosition("과장");               // position 우선
        assertThat(om.valueToTree(EngineerRow.from(u)).get("position").asText()).isEqualTo("과장");
    }

    // ===== requesterSearch =====
    @Test
    void requesterRow_matchesLegacy_includingNulls() {
        PersonInfo p = new PersonInfo();
        p.setId(11L);
        p.setUserNm("홍길동");
        p.setOrgNm("강진군청");
        p.setDeptNm("정보과");
        p.setPos("주무관");
        p.setTel("010-0000-0000");
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getUserNm());
        m.put("org", p.getOrgNm());
        m.put("dept", p.getDeptNm());
        m.put("pos", p.getPos());
        m.put("tel", p.getTel());
        assertTreeEqual(RequesterRow.from(p), m);

        PersonInfo empty = new PersonInfo();
        empty.setId(12L);                    // 나머지 null
        JsonNode j = om.valueToTree(RequesterRow.from(empty));
        assertThat(j.size()).isEqualTo(6);
        assertThat(j.has("tel")).isTrue();
        assertThat(j.get("tel").isNull()).isTrue();
    }

    // ===== partnerContactSearch =====
    @Test
    void partnerContactRow_matchesLegacy_andOrgNullWhenNoPartner() {
        Partner partner = new Partner();
        partner.setName("ABC시스템");
        PartnerContact c = new PartnerContact();
        c.setContactId(21L);
        c.setName("김담당");
        c.setPartner(partner);
        c.setPosition("팀장");
        c.setTel("02-1111-2222");
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getContactId());
        m.put("name", c.getName());
        m.put("org", c.getPartner() != null ? c.getPartner().getName() : null);
        m.put("pos", c.getPosition());
        m.put("tel", c.getTel());
        assertTreeEqual(PartnerContactRow.from(c), m);

        c.setPartner(null);                  // org → null
        JsonNode j = om.valueToTree(PartnerContactRow.from(c));
        assertThat(j.has("org")).isTrue();
        assertThat(j.get("org").isNull()).isTrue();
    }

    // ===== partnerSearch =====
    @Test
    void partnerRow_matchesLegacy() {
        Partner p = new Partner();
        p.setPartnerId(31L);
        p.setName("ABC시스템");
        p.setPartnerType("유지보수");
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getPartnerId());
        m.put("name", p.getName());
        m.put("type", p.getPartnerType());
        assertTreeEqual(PartnerRow.from(p), m);
    }

    // ===== docPartners =====
    @Test
    void docPartnerRow_matchesLegacy_snakeCase_andNameFallback() {
        OpsDocPartner dp = new OpsDocPartner();
        dp.setPartnerId(41L);
        dp.setRoleLabel("주관");
        Partner p = new Partner();
        p.setName("ABC시스템");
        Map<String, Object> m = new HashMap<>();
        m.put("partner_id", dp.getPartnerId());
        m.put("name", p.getName());
        m.put("role_label", dp.getRoleLabel());
        assertTreeEqual(DocPartnerRow.from(dp, p), m);

        // partner 없음 → name = "#id"
        JsonNode j = om.valueToTree(DocPartnerRow.from(dp, null));
        assertThat(j.get("name").asText()).isEqualTo("#41");
        assertThat(j.has("partner_id")).isTrue();      // snake_case 키 보존
        assertThat(j.has("role_label")).isTrue();
    }

    // ===== staffSearch =====
    @Test
    void staffRow_matchesLegacy_orgResolvedArg() {
        Staff s = new Staff();
        s.setStaffId(51L);
        s.setName("서현규");
        s.setPosition("선임");
        String unit = "SW지원팀";
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getStaffId());
        m.put("name", s.getName());
        m.put("org", unit);
        m.put("pos", s.getPosition());
        assertTreeEqual(StaffRow.of(s, unit), m);

        // org null (orgUnit 미해결)
        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", s.getStaffId());
        m2.put("name", s.getName());
        m2.put("org", null);
        m2.put("pos", s.getPosition());
        assertTreeEqual(StaffRow.of(s, null), m2);
    }
}
