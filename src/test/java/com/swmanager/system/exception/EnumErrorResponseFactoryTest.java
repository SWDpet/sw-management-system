package com.swmanager.system.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * EnumErrorResponseFactory 단위 테스트.
 *
 * 기획서/개발계획서 매핑:
 *  - FR-A5 / §5-7-3: 400 응답 포맷 (code, field, enumType, allowed, path)
 *  - §5-7-4: MATM vs HMNR 별 allowed 추출 규칙 + UNKNOWN 처리
 *  - 개발계획서 T1-1, T1-2: 핵심 커버리지
 */
class EnumErrorResponseFactoryTest {

    @Test
    void from_MATM_withEnumRequiredType_includesAllowedArray() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/document/api/status/1");
        // MethodArgumentTypeMismatchException 은 mock 으로 생성 (requiredType = DocumentStatus.class)
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "FOO", DocumentStatus.class, "status", null, new RuntimeException());

        ResponseEntity<Map<String, Object>> resp = EnumErrorResponseFactory.from(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("ENUM_VALUE_NOT_ALLOWED");
        assertThat(body.get("field")).isEqualTo("status");
        assertThat(body.get("enumType")).isEqualTo(DocumentStatus.class.getName());
        assertThat(body.get("path")).isEqualTo("/document/api/status/1");

        @SuppressWarnings("unchecked")
        List<String> allowed = (List<String>) body.get("allowed");
        assertThat(allowed).containsExactlyInAnyOrder("DRAFT", "COMPLETED");
    }

    @Test
    void from_MATM_withNonEnumRequiredType_setsEnumTypeUnknownAndOmitsAllowed() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", String.class, "param", null, new RuntimeException());

        Map<String, Object> body = EnumErrorResponseFactory.from(ex, req).getBody();

        assertThat(body).isNotNull();
        assertThat(body.get("enumType")).isEqualTo("UNKNOWN");
        assertThat(body).doesNotContainKey("allowed");  // §5-7-4 UNKNOWN 케이스
    }

    @Test
    void from_HMNR_withInvalidFormatException_includesAllowedFromTargetType() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/document/api/save");
        // InvalidFormatException → HttpMessageNotReadableException 래핑
        InvalidFormatException ife = InvalidFormatException.from(
                null, "bad", "UNKNOWN_TYPE", DocumentType.class);
        ife.prependPath("root", "docType");
        HttpMessageNotReadableException hmnr = new HttpMessageNotReadableException(
                "bad enum", ife, new MockHttpInputMessage(new byte[0]));

        Map<String, Object> body = EnumErrorResponseFactory.from(hmnr, req).getBody();

        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("ENUM_VALUE_NOT_ALLOWED");
        assertThat(body.get("field")).isEqualTo("docType");
        assertThat(body.get("enumType")).isEqualTo(DocumentType.class.getName());

        @SuppressWarnings("unchecked")
        List<String> allowed = (List<String>) body.get("allowed");
        assertThat(allowed).containsExactlyInAnyOrder(
                "COMMENCE", "INTERIM", "COMPLETION", "INSPECT",
                "FAULT", "SUPPORT", "INSTALL", "PATCH");
    }

    @Test
    void from_HMNR_withoutInvalidFormatException_setsEnumTypeUnknown() {
        HttpMessageNotReadableException hmnr = new HttpMessageNotReadableException(
                "bad", new MockHttpInputMessage(new byte[0]));

        HttpServletRequest req = mock(HttpServletRequest.class);
        Map<String, Object> body = EnumErrorResponseFactory.from(hmnr, req).getBody();

        assertThat(body).isNotNull();
        assertThat(body.get("enumType")).isEqualTo("UNKNOWN");
        assertThat(body).doesNotContainKey("allowed");
    }
}
