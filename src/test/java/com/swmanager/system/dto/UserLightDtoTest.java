package com.swmanager.system.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.User;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserLightDto 직렬화 골든 테스트 (userapi-light-dto §6-4).
 *
 * 기존 UserApiController.toLightDto() 의 LinkedHashMap 응답조립을 record 로 바꾸면서,
 * /api/users 경량 조회 응답 JSON 이 무손실(키셋·값·키순서·null 포함·마스킹 6필드)인지 고정한다.
 */
class UserLightDtoTest {

    private final ObjectMapper om = new ObjectMapper();

    /** 치환 전 toLightDto() 가 만들던 LinkedHashMap 을 그대로 복제(골든 기준). */
    private Map<String, Object> legacy(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", u.getUserSeq());
        m.put("userid", u.getUserid());
        m.put("username", u.getUsername());
        m.put("deptNm", u.getDeptNm());
        m.put("positionTitle", u.getPositionTitle());
        m.put("enabled", u.isEnabled());
        return m;
    }

    private User fullUser() {
        User u = new User();
        u.setUserSeq(7L);
        u.setUserid("ukjin");
        u.setUsername("박욱진");
        u.setDeptNm("SW지원부");
        u.setPositionTitle("대리");
        u.setEnabled(true);
        return u;
    }

    @Test
    void treeAndStringEqualLegacy() throws Exception {
        User u = fullUser();
        JsonNode dto = om.valueToTree(UserLightDto.from(u));
        JsonNode legacy = om.valueToTree(legacy(u));
        assertThat(dto).isEqualTo(legacy);                                  // 키셋·값
        assertThat(om.writeValueAsString(UserLightDto.from(u)))
                .isEqualTo(om.writeValueAsString(legacy(u)));               // 키순서(LinkedHashMap 결정적)
    }

    @Test
    void enabledKey_isBooleanTrueFalse() {
        JsonNode on = om.valueToTree(UserLightDto.from(fullUser()));
        assertThat(on.has("enabled")).isTrue();           // "enabled" 키(is-접두 깎임 없음)
        assertThat(on.get("enabled").isBoolean()).isTrue();
        assertThat(on.get("enabled").asBoolean()).isTrue();

        User off = fullUser();
        off.setEnabled(false);
        assertThat(om.valueToTree(UserLightDto.from(off)).get("enabled").asBoolean()).isFalse();

        User nullEnabled = fullUser();
        nullEnabled.setEnabled(null);                     // isEnabled()=false(null 불가 primitive)
        assertThat(om.valueToTree(UserLightDto.from(nullEnabled)).get("enabled").asBoolean()).isFalse();
    }

    @Test
    void maskingPreserved_exactlySixFields_noSensitive() {
        JsonNode j = om.valueToTree(UserLightDto.from(fullUser()));
        assertThat(j.size()).isEqualTo(6);                // 정확히 6필드
        assertThat(j.has("tel")).isFalse();               // 마스킹 대상 미반환
        assertThat(j.has("mobile")).isFalse();
        assertThat(j.has("email")).isFalse();
    }

    @Test
    void nullFields_includedAsNull() {
        User u = new User();
        u.setUserSeq(1L);                                 // 나머지 null
        JsonNode j = om.valueToTree(UserLightDto.from(u));
        assertThat(j.has("deptNm")).isTrue();
        assertThat(j.get("deptNm").isNull()).isTrue();
        assertThat(j.get("enabled").asBoolean()).isFalse();   // null enabled → false
        JsonNode legacyJson = om.valueToTree(legacy(u));
        assertThat(j).isEqualTo(legacyJson);
    }
}
