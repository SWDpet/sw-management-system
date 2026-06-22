package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.User;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserInfoRow(13키) / UserInfoSecureRow(16키) 직렬화 골든 테스트 (document-userinfo-rows-dto §6-4).
 *
 * DocumentController.getUserInfo / getUserInfoSecure 의 컨트롤러-로컬 HashMap 응답조립을 record 로
 * 바꾸면서 응답 JSON 무손실(키셋·값·null·민감 화이트리스트)을 고정한다. JsonNode tree 동치.
 */
class UserInfoRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private User sampleUser() {
        User u = new User();
        u.setUserSeq(11L);
        u.setUsername("홍길동");
        u.setPositionTitle("책임");
        u.setPosition("대리");
        u.setTechGrade("중급기술자");
        u.setMobile("010-1111-2222");
        u.setTel("02-333-4444");
        u.setAddress("서울시 강남구");
        u.setTasks("UPIS,KRAS");
        u.setDeptNm("SW지원부");
        u.setTeamNm("SW지원팀");
        u.setCareerYears("7");
        u.setFieldRole("PM");
        u.setSsn("900101-1******");
        u.setCertificate("정보처리기사");
        u.setEmail("hong@example.com");
        return u;
    }

    private Map<String, Object> legacyBase(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("userSeq", u.getUserSeq());
        m.put("username", u.getUsername());
        m.put("positionTitle", u.getPositionTitle());
        m.put("position", u.getPosition());
        m.put("techGrade", u.getTechGrade());
        m.put("mobile", u.getMobile());
        m.put("tel", u.getTel());
        m.put("address", u.getAddress());
        m.put("tasks", u.getTasks());
        m.put("deptNm", u.getDeptNm());
        m.put("teamNm", u.getTeamNm());
        m.put("careerYears", u.getCareerYears());
        m.put("fieldRole", u.getFieldRole());
        return m;
    }

    @Test
    void userInfoRow_13keys_noSensitive() {
        User u = sampleUser();
        JsonNode a = om.valueToTree(UserInfoRow.from(u));
        JsonNode b = om.valueToTree(legacyBase(u));
        assertThat(a).isEqualTo(b);
        assertThat(a.size()).isEqualTo(13);
        // 민감 화이트리스트: 비-secure 는 ssn/certificate/email 키 부재
        assertThat(a.has("ssn")).isFalse();
        assertThat(a.has("certificate")).isFalse();
        assertThat(a.has("email")).isFalse();
    }

    @Test
    void userInfoSecureRow_16keys_withSensitive() {
        User u = sampleUser();
        Map<String, Object> legacy = legacyBase(u);
        legacy.put("ssn", u.getSsn());
        legacy.put("certificate", u.getCertificate());
        legacy.put("email", u.getEmail());

        JsonNode a = om.valueToTree(UserInfoSecureRow.from(u));
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.size()).isEqualTo(16);
        assertThat(a.get("ssn").asText()).isEqualTo("900101-1******");
    }

    @Test
    void nullFields_preserved() {
        User u = new User();
        u.setUserSeq(12L);   // 나머지 null
        JsonNode a = om.valueToTree(UserInfoRow.from(u));
        assertThat(a.size()).isEqualTo(13);
        assertThat(a.has("username")).isTrue();
        assertThat(a.get("username").isNull()).isTrue();
    }
}
