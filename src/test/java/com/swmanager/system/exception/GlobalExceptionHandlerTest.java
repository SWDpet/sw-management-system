package com.swmanager.system.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 단위테스트 (커버리지 상향 beyond-A, 순수).
 * MockHttpServletRequest/Response 로 핸들러를 직접 호출해 API(JSON)/web(HTML) 양분기 검증.
 * enum-mismatch 2핸들러(handleEnumTypeMismatch/handleJsonEnumMismatch)는 EnumErrorResponseFactory 에
 * 위임하며 ResponseEntity&lt;Map&gt; 400 을 반환 — 2026-06-28 logUnknownEnum 분기 포함 보강(beyond-A).
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

    // ===== enum 바인딩 핸들러 + logUnknownEnum (beyond-A) =====

    /** 핸들러 반환은 EnumErrorResponseFactory 의 ResponseEntity<Map> (ApiResponse 아님). */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mapBodyOf(Object handlerResult) {
        assertThat(handlerResult).isInstanceOf(ResponseEntity.class);
        return (Map<String, Object>) ((ResponseEntity<?>) handlerResult).getBody();
    }

    private static Logger enumMonitorLogger() {
        return (Logger) LoggerFactory.getLogger("com.swmanager.system.monitoring.EnumBindingMonitor");
    }

    /** logUnknownEnum 의 모니터링 로그(EnumBindingMonitor)를 포착하는 ListAppender 부착. 레벨 고정으로 임계값 비의존. */
    private ListAppender<ILoggingEvent> attachEnumMonitorAppender() {
        Logger monitor = enumMonitorLogger();
        monitor.setLevel(Level.TRACE);   // 설정 임계값과 무관하게 캡처 보장
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        monitor.addAppender(appender);
        return appender;
    }

    private void detachEnumMonitorAppender(ListAppender<ILoggingEvent> appender) {
        Logger monitor = enumMonitorLogger();
        monitor.detachAppender(appender);
        appender.stop();           // start() 대칭 — 라이프사이클 정리
        monitor.setLevel(null);    // 상속 레벨로 복원
    }

    /** MethodArgumentNotValidException 의 실 MethodParameter 용 더미 타깃(mock getMessage NPE 회피). */
    @SuppressWarnings("unused")
    private void dummyValidationTarget(String name) { }

    /** C1: MethodArgumentTypeMismatch(enum) → 400 allowed + logUnknownEnum(enumType non-null, principal) MDC 단언. */
    @Test
    void enumTypeMismatch_returnsBadRequestWithAllowed() {
        ListAppender<ILoggingEvent> appender = attachEnumMonitorAppender();
        try {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/document/api/status/1");
            req.setUserPrincipal(() -> "alice");   // logUnknownEnum userid: principal present 분기
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "FOO", DocumentStatus.class, "status", null, new RuntimeException());

            Object res = handler.handleEnumTypeMismatch(ex, req);
            assertThat(statusOf(res)).isEqualTo(400);
            Map<String, Object> body = mapBodyOf(res);
            assertThat(body.get("code")).isEqualTo("ENUM_VALUE_NOT_ALLOWED");
            assertThat(body.get("field")).isEqualTo("status");
            assertThat(body.get("enumType")).isEqualTo(DocumentStatus.class.getName());
            assertThat(body.get("path")).isEqualTo("/document/api/status/1");
            @SuppressWarnings("unchecked")
            List<String> allowed = (List<String>) body.get("allowed");
            assertThat(allowed).containsExactlyInAnyOrder("DRAFT", "COMPLETED");

            // logUnknownEnum 이 실제로 호출됐는지: 모니터링 로그 1건 + MDC 스냅샷(이벤트 시점) 단언
            assertThat(appender.list).hasSize(1);
            ILoggingEvent event = appender.list.get(0);
            assertThat(event.getMDCPropertyMap())
                    .containsEntry("eventKey", "UNKNOWN_ENUM_VALUE")
                    .containsEntry("enumType", DocumentStatus.class.getName())
                    .containsEntry("inputValue", "FOO")
                    .containsEntry("endpoint", "GET /document/api/status/1")
                    .containsEntry("userid", "alice");   // principal present 분기
            // finally 블록 정리 — 핸들러 종료 후 MDC 누수 없음
            assertThat(MDC.get("eventKey")).isNull();
        } finally {
            detachEnumMonitorAppender(appender);
            MDC.clear();   // 단언 실패 시에도 스레드 MDC 격리 보장
        }
    }

    /** C2: HttpMessageNotReadable(InvalidFormatException 원인) → 400 allowed + targetType 추출. */
    @Test
    void jsonEnumMismatch_withInvalidFormat_returnsBadRequest() throws Exception {
        InvalidFormatException ife = InvalidFormatException.from(
                null, "bad", "UNKNOWN_TYPE", DocumentType.class);
        ife.prependPath("root", "docType");
        HttpMessageNotReadableException hmnr = new HttpMessageNotReadableException(
                "bad enum", ife, new MockHttpInputMessage(new byte[0]));
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/document/api/save");

        Object res = handler.handleJsonEnumMismatch(hmnr, req);
        assertThat(statusOf(res)).isEqualTo(400);
        Map<String, Object> body = mapBodyOf(res);
        assertThat(body.get("code")).isEqualTo("ENUM_VALUE_NOT_ALLOWED");
        assertThat(body.get("field")).isEqualTo("docType");
        assertThat(body.get("enumType")).isEqualTo(DocumentType.class.getName());
        @SuppressWarnings("unchecked")
        List<String> allowed = (List<String>) body.get("allowed");
        assertThat(allowed).containsExactlyInAnyOrder("COMMENCE", "INTERIM", "COMPLETION");
    }

    /** C3: HttpMessageNotReadable(비-IFE 원인) → enumType UNKNOWN + logUnknownEnum(enumType null, anonymous) MDC 단언. */
    @Test
    void jsonEnumMismatch_withoutInvalidFormat_returnsUnknown() {
        ListAppender<ILoggingEvent> appender = attachEnumMonitorAppender();
        try {
            HttpMessageNotReadableException hmnr = new HttpMessageNotReadableException(
                    "bad", new RuntimeException("x"), new MockHttpInputMessage(new byte[0]));
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");  // principal 없음 → anonymous

            Object res = handler.handleJsonEnumMismatch(hmnr, req);
            assertThat(statusOf(res)).isEqualTo(400);
            Map<String, Object> body = mapBodyOf(res);
            assertThat(body.get("enumType")).isEqualTo("UNKNOWN");
            assertThat(body).doesNotContainKey("allowed");

            // logUnknownEnum: targetType=null → MDC enumType="UNKNOWN", principal 없음 → userid="anonymous"
            assertThat(appender.list).hasSize(1);
            ILoggingEvent event = appender.list.get(0);
            assertThat(event.getMDCPropertyMap())
                    .containsEntry("eventKey", "UNKNOWN_ENUM_VALUE")
                    .containsEntry("enumType", "UNKNOWN")          // enumType null 분기
                    .containsEntry("endpoint", "POST /x")
                    .containsEntry("userid", "anonymous");         // anonymous 분기
            assertThat(MDC.get("enumType")).isNull();
        } finally {
            detachEnumMonitorAppender(appender);
            MDC.clear();
        }
    }

    /** C5: handleValidationException 의 MethodArgumentNotValidException 분기. */
    @Test
    void validation_methodArgumentNotValid_apiAndWeb() throws Exception {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Form(), "form");
        br.rejectValue("name", "NotBlank", "이름은 필수입니다.");
        // 실 MethodParameter — getMessage() 가 호출돼도 NPE 없는 안전한 fixture
        MethodParameter param = new MethodParameter(
                getClass().getDeclaredMethod("dummyValidationTarget", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, br);

        Object api = handler.handleValidationException(ex, apiReq(), new MockHttpServletResponse());
        assertThat(statusOf(api)).isEqualTo(400);
        assertThat(bodyOf(api).getMessage()).isEqualTo("이름은 필수입니다.");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertThat(handler.handleValidationException(ex, webReq(), resp)).isNull();
        assertThat(resp.getStatus()).isEqualTo(400);
    }
}
