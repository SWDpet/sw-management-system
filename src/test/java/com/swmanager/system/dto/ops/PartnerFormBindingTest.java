package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PartnerForm / ContactForm 요청 바디 바인딩 골든 테스트 (partner-request-dto §6-4).
 *
 * 기존 비타입 Map 수신을 타입 DTO 로 바꾸면서, 프론트(partner-management.html)가 보내던
 * JSON 키(snake_case 포함)를 무손실 바인딩하는지 고정한다.
 */
class PartnerFormBindingTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void partnerForm_bindsSnakeCaseKeys() throws Exception {
        // partner-management.html:85 본문
        String json = "{\"name\":\"가나테크\",\"partner_type\":\"개발\",\"main_tel\":\"02-1\","
                + "\"biz_no\":\"123-45\",\"note\":\"비고\"}";

        PartnerForm f = om.readValue(json, PartnerForm.class);

        assertThat(f.name()).isEqualTo("가나테크");
        assertThat(f.partnerType()).isEqualTo("개발");      // partner_type → partnerType
        assertThat(f.mainTel()).isEqualTo("02-1");          // main_tel → mainTel
        assertThat(f.bizNo()).isEqualTo("123-45");          // biz_no → bizNo
        assertThat(f.note()).isEqualTo("비고");
    }

    @Test
    void contactForm_bindsKeys_emailOmittedNull() throws Exception {
        // partner-management.html:94 본문 (email 미전송)
        ContactForm f = om.readValue("{\"name\":\"홍길동\",\"position\":\"팀장\",\"tel\":\"010-2\"}", ContactForm.class);

        assertThat(f.name()).isEqualTo("홍길동");
        assertThat(f.position()).isEqualTo("팀장");
        assertThat(f.tel()).isEqualTo("010-2");
        assertThat(f.email()).isNull();                     // 미전송 → null (현행 동일)
    }

    @Test
    void unknownKeys_ignored() throws Exception {
        PartnerForm f = om.readValue("{\"name\":\"x\",\"legacy_col\":1}", PartnerForm.class);
        assertThat(f.name()).isEqualTo("x");
    }
}
