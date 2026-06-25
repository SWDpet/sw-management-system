package com.swmanager.system.exception;

import com.swmanager.system.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 단위테스트 (커버리지 상향 beyond-A, 순수).
 * MockHttpServletRequest/Response 로 핸들러를 직접 호출해 API(JSON)/web(HTML) 양분기 검증.
 * enum-mismatch 2핸들러(EnumErrorResponseFactory 위임)는 best-effort 제외.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest apiReq() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRequestURI("/some/page");
        r.addHeader("Accept", "application/json");
        return r;
    }

    private MockHttpServletRequest webReq() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRequestURI("/some/page");          // /api/ 아니고 Accept json 아님 → web 분기
        return r;
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<Object> bodyOf(Object handlerResult) {
        assertThat(handlerResult).isInstanceOf(ResponseEntity.class);
        return (ApiResponse<Object>) ((ResponseEntity<?>) handlerResult).getBody();
    }

    private int statusOf(Object handlerResult) {
        return ((ResponseEntity<?>) handlerResult).getStatusCode().value();
    }

    // ===== handleBusinessException =====

    @Test
    void businessException_api() throws Exception {
        Object res = handler.handleBusinessException(
                new BusinessException(ErrorCode.DUPLICATE_PROJECT), apiReq(), new MockHttpServletResponse());
        assertThat(statusOf(res)).isEqualTo(409);
        ApiResponse<Object> body = bodyOf(res);
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getErrorCode()).isEqualTo("D003");
    }

    @Test
    void businessException_web_writesHtml() throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        Object res = handler.handleBusinessException(
                new BusinessException(ErrorCode.DUPLICATE_PROJECT), webReq(), resp);
        assertThat(res).isNull();
        assertThat(resp.getStatus()).isEqualTo(409);
        assertThat(resp.getContentType()).contains("text/html");
        assertThat(resp.getContentAsString()).contains("D003");   // getErrorHtml 에 코드 포함
    }

    // ===== handleResourceNotFound =====

    @Test
    void resourceNotFound_apiAndWeb() throws Exception {
        ResourceNotFoundException ex = new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND);
        assertThat(statusOf(handler.handleResourceNotFound(ex, apiReq(), new MockHttpServletResponse()))).isEqualTo(404);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleResourceNotFound(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(404);
    }

    // ===== handleAccessDenied =====

    @Test
    void accessDenied_apiAndWeb() throws Exception {
        AccessDeniedException ex = new AccessDeniedException("denied");
        ApiResponse<Object> body = bodyOf(handler.handleAccessDenied(ex, apiReq(), new MockHttpServletResponse()));
        assertThat(body.getErrorCode()).isEqualTo("A003");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleAccessDenied(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(403);
    }

    // ===== handleValidationException (BindException) =====

    /** rejectValue 가 bean 프로퍼티를 해석하므로 실제 필드를 가진 타깃 사용. */
    static class Form { private String name; public String getName() { return name; } public void setName(String n) { this.name = n; } }

    @Test
    void validation_bindException_apiAndWeb() throws Exception {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Form(), "form");
        br.rejectValue("name", "NotBlank", "이름은 필수입니다.");   // FieldError → getFieldErrors 로깅 람다 커버
        BindException ex = new BindException(br);

        Object api = handler.handleValidationException(ex, apiReq(), new MockHttpServletResponse());
        assertThat(statusOf(api)).isEqualTo(400);
        assertThat(bodyOf(api).getMessage()).isEqualTo("이름은 필수입니다.");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleValidationException(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(400);
    }

    // ===== handleConstraintViolation =====

    @Test
    void constraintViolation_emptySet_apiAndWeb() throws Exception {
        ConstraintViolationException ex = new ConstraintViolationException(Set.of());
        Object api = handler.handleConstraintViolation(ex, apiReq(), new MockHttpServletResponse());
        assertThat(statusOf(api)).isEqualTo(400);
        assertThat(bodyOf(api).getMessage()).isEqualTo("입력값이 올바르지 않습니다.");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleConstraintViolation(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(400);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void constraintViolation_withMockViolation_apiAndWeb() throws Exception {
        ConstraintViolation<?> v = mock(ConstraintViolation.class);
        when(v.getPropertyPath()).thenReturn(mock(Path.class));
        when(v.getMessage()).thenReturn("must not be null");
        ConstraintDescriptor cd = mock(ConstraintDescriptor.class);
        Annotation ann = mock(Annotation.class);
        when(ann.annotationType()).thenReturn((Class) NotNull.class);
        when(cd.getAnnotation()).thenReturn(ann);
        when(v.getConstraintDescriptor()).thenReturn(cd);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v));
        Object api = handler.handleConstraintViolation(ex, apiReq(), new MockHttpServletResponse());
        assertThat(statusOf(api)).isEqualTo(400);
        assertThat(bodyOf(api).getMessage()).isEqualTo("must not be null");   // 첫 violation 메시지
    }

    // ===== handleNotFound (NoHandlerFoundException) =====

    @Test
    void noHandlerFound_apiAndWeb() throws Exception {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/missing", new HttpHeaders());
        assertThat(statusOf(handler.handleNotFound(ex, apiReq(), new MockHttpServletResponse()))).isEqualTo(404);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleNotFound(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(404);
    }

    // ===== handleNoResourceFound (void, no-throw) =====

    @Test
    void noResourceFound_noThrow() {
        assertThatCode(() -> handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/favicon.ico"))).doesNotThrowAnyException();
    }

    // ===== handleException (generic) =====

    @Test
    void genericException_apiAndWeb() throws Exception {
        RuntimeException ex = new RuntimeException("boom");
        ApiResponse<Object> body = bodyOf(handler.handleException(ex, apiReq(), new MockHttpServletResponse()));
        assertThat(body.getErrorCode()).isEqualTo("S001");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleException(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(500);
    }

    // ===== isApiRequest: /api/ 경로(Accept 없이도 API 분기) =====

    @Test
    void isApiRequest_viaApiPath() throws Exception {
        MockHttpServletRequest apiPath = new MockHttpServletRequest();
        apiPath.setRequestURI("/api/anything");   // Accept 헤더 없음 → 경로로 API 판정
        Object res = handler.handleException(new RuntimeException("x"), apiPath, new MockHttpServletResponse());
        assertThat(res).isInstanceOf(ResponseEntity.class);
        assertThat(statusOf(res)).isEqualTo(500);
    }
}
