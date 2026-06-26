package com.swmanager.system.controller.inspection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swmanager.system.domain.User;
import com.swmanager.system.dto.inspection.InspectionQrBatchRequest;
import com.swmanager.system.dto.inspection.InspectionQrBatchResponse;
import com.swmanager.system.dto.inspection.SiteNotMappedError;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.inspection.InspectionQrBatchService;
import com.swmanager.system.service.inspection.SiteNotMappedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InspectionQrBatchController 단위 테스트 (beyond-A — coverage-misc-controllers).
 * 생성자 주입(service·ObjectMapper·Validator). normalize(static)는 실 ObjectNode 로 실호출,
 * treeToValue/validate/service.upload 는 mock 으로 분기 결정적 커버 + @ExceptionHandler 422.
 */
class InspectionQrBatchControllerTest {

    private InspectionQrBatchService service;
    private ObjectMapper objectMapper;
    private Validator validator;
    private InspectionQrBatchController controller;

    @BeforeEach
    void setUp() {
        service = mock(InspectionQrBatchService.class);
        objectMapper = mock(ObjectMapper.class);
        validator = mock(Validator.class);
        controller = new InspectionQrBatchController(service, objectMapper, validator);
    }

    private static ObjectNode body() {
        // normalize(static)가 동작할 실제 mutable ObjectNode (별도 real mapper)
        return new ObjectMapper().createObjectNode();
    }

    private static CustomUserDetails user(long seq) {
        User u = new User();
        u.setUserSeq(seq);
        u.setUserid("tester");
        u.setUserRole("ROLE_USER");
        return new CustomUserDetails(u);
    }

    @Test
    void upload_ok_returns200_withUserId() throws Exception { // T-Q1
        ObjectNode raw = body();
        InspectionQrBatchRequest req = mock(InspectionQrBatchRequest.class);
        InspectionQrBatchResponse res = mock(InspectionQrBatchResponse.class);
        when(objectMapper.treeToValue(any(com.fasterxml.jackson.databind.JsonNode.class),
                eq(InspectionQrBatchRequest.class))).thenReturn(req);
        when(validator.validate(req)).thenReturn(Set.of());
        when(service.upload(req, 7L)).thenReturn(res);

        ResponseEntity<InspectionQrBatchResponse> out = controller.upload(raw, user(7L));
        assertThat(out.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(out.getBody()).isSameAs(res); // 서비스 응답 본문 전파
        verify(service).upload(req, 7L);
    }

    @Test
    void upload_meNull_usesNullUserId() throws Exception { // T-Q4
        ObjectNode raw = body();
        InspectionQrBatchRequest req = mock(InspectionQrBatchRequest.class);
        when(objectMapper.treeToValue(any(com.fasterxml.jackson.databind.JsonNode.class),
                eq(InspectionQrBatchRequest.class))).thenReturn(req);
        when(validator.validate(req)).thenReturn(Set.of());
        InspectionQrBatchResponse res = mock(InspectionQrBatchResponse.class);
        when(service.upload(req, null)).thenReturn(res);

        ResponseEntity<InspectionQrBatchResponse> out = controller.upload(raw, null);
        assertThat(out.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(out.getBody()).isSameAs(res);
        verify(service).upload(req, null);
    }

    @Test
    void upload_malformed_throwsIllegalArgument() throws Exception { // T-Q2
        ObjectNode raw = body();
        when(objectMapper.treeToValue(any(com.fasterxml.jackson.databind.JsonNode.class),
                eq(InspectionQrBatchRequest.class))).thenThrow(new RuntimeException("bad json"));
        assertThatThrownBy(() -> controller.upload(raw, user(1L)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(service, never()).upload(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void upload_validationFails_throwsConstraintViolation() throws Exception { // T-Q3
        ObjectNode raw = body();
        InspectionQrBatchRequest req = mock(InspectionQrBatchRequest.class);
        when(objectMapper.treeToValue(any(com.fasterxml.jackson.databind.JsonNode.class),
                eq(InspectionQrBatchRequest.class))).thenReturn(req);
        ConstraintViolation<InspectionQrBatchRequest> v = mock(ConstraintViolation.class);
        when(validator.validate(req)).thenReturn(Set.of(v));

        assertThatThrownBy(() -> controller.upload(raw, user(1L)))
                .isInstanceOf(ConstraintViolationException.class);
        verify(service, never()).upload(any(), any());
    }

    @Test
    void handleSiteNotMapped_returns422() { // T-Q5
        ResponseEntity<SiteNotMappedError> out =
                controller.handleSiteNotMapped(new SiteNotMappedException("SITE-123"));
        assertThat(out.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(out.getBody().error()).isEqualTo("site_not_mapped");
        assertThat(out.getBody().site()).isEqualTo("SITE-123");
    }
}
