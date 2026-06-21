package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.ContractParticipant;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContractParticipantRow / SecureContractParticipantRow 직렬화 골든 테스트 (participant-rows-dto §6-4).
 *
 * 기존 DocumentParticipantController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 바꾸면서, 응답 JSON 의
 * 키셋·값·null 포함·폴백을 무손실 보존하고 ★민감 필드(ssn/certificate) 분리(감사 P1-3)를 유지하는지 고정한다.
 * 현행이 HashMap 이라 키순서는 비결정 → JsonNode tree 동치만 검증.
 */
class ParticipantRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Map<String, Object> legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    private ContractParticipant withUser() {
        User u = new User();
        u.setUserSeq(7L);
        u.setUsername("박욱진");
        u.setPositionTitle("대리");
        u.setTasks("설계,개발");
        u.setSsn("900101-1******");
        u.setCertificate("정보처리기사");
        ContractParticipant cp = new ContractParticipant();
        cp.setParticipantId(3);
        cp.setUser(u);
        cp.setRoleType("PARTICIPANT");
        cp.setTechGrade("고급");
        cp.setTaskDesc("DB 설계");
        cp.setIsSiteRep(true);
        return cp;
    }

    private Map<String, Object> legacyCommon(ContractParticipant cp) {
        User u = cp.getUser();
        Map<String, Object> m = new HashMap<>();
        m.put("participantId", cp.getParticipantId());
        m.put("userId", u != null ? u.getUserSeq() : null);
        m.put("userName", u != null ? u.getUsername() : "");
        m.put("position", u != null ? u.getPositionTitle() : "");
        m.put("roleType", cp.getRoleType());
        m.put("techGrade", cp.getTechGrade());
        m.put("taskDesc", cp.getTaskDesc());
        m.put("isSiteRep", cp.getIsSiteRep());
        m.put("tasks", u != null ? u.getTasks() : "");
        return m;
    }

    @Test
    void contractParticipantRow_matchesLegacy_andHasNoSensitiveFields() {
        ContractParticipant cp = withUser();
        assertTreeEqual(ContractParticipantRow.from(cp), legacyCommon(cp));

        JsonNode j = om.valueToTree(ContractParticipantRow.from(cp));
        assertThat(j.size()).isEqualTo(9);                 // 정확히 9키
        assertThat(j.has("ssn")).isFalse();                // ★민감 필드 미포함(누출 차단)
        assertThat(j.has("certificate")).isFalse();
        assertThat(j.has("isSiteRep")).isTrue();           // is-접두 깎임 없음
        assertThat(j.get("isSiteRep").asBoolean()).isTrue();
    }

    @Test
    void secureContractParticipantRow_matchesLegacy_andHasSensitiveFields() {
        ContractParticipant cp = withUser();
        Map<String, Object> m = legacyCommon(cp);
        m.put("ssn", cp.getUser().getSsn());
        m.put("certificate", cp.getUser().getCertificate());
        assertTreeEqual(SecureContractParticipantRow.from(cp), m);

        JsonNode j = om.valueToTree(SecureContractParticipantRow.from(cp));
        assertThat(j.size()).isEqualTo(11);                // 9 + ssn + certificate
        assertThat(j.get("ssn").asText()).isEqualTo("900101-1******");
        assertThat(j.get("certificate").asText()).isEqualTo("정보처리기사");
    }

    @Test
    void userNull_fallbacksPreserved() {
        ContractParticipant cp = new ContractParticipant();
        cp.setParticipantId(4);
        cp.setRoleType("PARTICIPANT");
        cp.setIsSiteRep(false);                            // user=null

        JsonNode j = om.valueToTree(ContractParticipantRow.from(cp));
        assertThat(j.get("userId").isNull()).isTrue();     // user null → userId null
        assertThat(j.get("userName").asText()).isEmpty();  // → "" (빈 문자열)
        assertThat(j.get("position").asText()).isEmpty();
        assertThat(j.get("tasks").asText()).isEmpty();
        assertThat(j.get("isSiteRep").asBoolean()).isFalse();
        assertTreeEqual(ContractParticipantRow.from(cp), legacyCommon(cp));

        // secure 도 동일 폴백 + ssn/certificate ""
        JsonNode js = om.valueToTree(SecureContractParticipantRow.from(cp));
        assertThat(js.get("ssn").asText()).isEmpty();
        assertThat(js.get("certificate").asText()).isEmpty();
    }
}
