package com.swmanager.system.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResult 직렬화 골든 테스트 (dto-migration Step 1).
 *
 * 기존 Map 응답 형태를 무손실 보존하는지 Jackson 직렬화로 고정:
 *  - @JsonInclude(NON_NULL) 로 null 필드(키) 생략.
 *  - error 가 코드형(맵)/메시지형(문자열) 양형으로 보존.
 */
class ApiResultTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void ok_noData_onlySuccessKey() throws Exception {
        assertThat(om.writeValueAsString(ApiResult.ok()))
                .isEqualTo("{\"success\":true}");
    }

    @Test
    void ok_withData_successAndData_noErrorKey() throws Exception {
        String json = om.writeValueAsString(ApiResult.ok(java.util.List.of(1, 2)));
        assertThat(json).isEqualTo("{\"success\":true,\"data\":[1,2]}");
        assertThat(json).doesNotContain("error");
    }

    @Test
    void fail_codeForm_errorIsObject_noDataKey() throws Exception {
        String json = om.writeValueAsString(ApiResult.fail("FORBIDDEN", "수정 권한이 없습니다"));
        assertThat(json).contains("\"success\":false");
        assertThat(json).contains("\"error\":{");
        assertThat(json).contains("\"code\":\"FORBIDDEN\"");
        assertThat(json).contains("\"message\":\"수정 권한이 없습니다\"");
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    void fail_noArg_onlySuccessFalse() throws Exception {
        // P7: error 없이 {success:false} (예: 404 미존재)
        assertThat(om.writeValueAsString(ApiResult.fail()))
                .isEqualTo("{\"success\":false}");
    }

    @Test
    void failMessage_errorIsString() throws Exception {
        String json = om.writeValueAsString(ApiResult.failMessage("문제가 발생했습니다"));
        assertThat(json).isEqualTo("{\"success\":false,\"error\":\"문제가 발생했습니다\"}");
    }

    @Test
    void ok_null_omitsDataKey_behavioralEquivalence() throws Exception {
        // find 미존재 prefill: 현행 {success:true,data:null} → ok(null) = {success:true}
        // 프론트(doc-inspect.html:830/1821)가 resp.data truthy 검사라 null/undefined 동일 분기.
        assertThat(om.writeValueAsString(ApiResult.ok(null)))
                .isEqualTo("{\"success\":true}");
    }
}
