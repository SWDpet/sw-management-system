package com.swmanager.system.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * 표준 JSON 응답 래퍼 (dto-migration 파일럿, S4 후속).
 *
 * 기존 컨트롤러의 {@code Map<String,Object>} 동적 조립(`result.put("success", ...)`)을
 * 타입 안전하게 대체한다. {@code @JsonInclude(NON_NULL)} 로 null 필드는 직렬화에서 생략하여
 * 현행 응답 형태를 보존한다.
 *
 * <ul>
 *   <li>성공:      {@code {"success":true}} 또는 {@code {"success":true,"data":...}}</li>
 *   <li>코드형 실패: {@code {"success":false,"error":{"code":...,"message":...}}} (예: 403)</li>
 *   <li>메시지 실패: {@code {"success":false,"error":"<message>"}} (일반 예외)</li>
 * </ul>
 *
 * ※ {@code error} 가 코드형(맵)/메시지형(문자열) 양형인 것은 기존 컨트롤러 응답을 충실히
 *   보존하기 위함이다. (기존 {@link ApiResponse} 는 {@code message/errorCode} 평면 구조로 형태가 다름)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult(Boolean success, Object data, Object error) {

    /** {@code {"success":true}} (data 없음 — 예: 삭제 성공) */
    public static ApiResult ok() {
        return new ApiResult(true, null, null);
    }

    /** {@code {"success":true,"data":...}} */
    public static ApiResult ok(Object data) {
        return new ApiResult(true, data, null);
    }

    /** {@code {"success":false}} — error 없이 실패 (예: 404 미존재. P7). */
    public static ApiResult fail() {
        return new ApiResult(false, null, null);
    }

    /** {@code {"success":false,"error":{"code":...,"message":...}}} — 권한거부 등 코드형. */
    public static ApiResult fail(String code, String message) {
        return new ApiResult(false, null, Map.of("code", code, "message", message));
    }

    /** {@code {"success":false,"error":"<message>"}} — 일반 예외(현행 result.put("error", e.getMessage())). */
    public static ApiResult failMessage(String message) {
        return new ApiResult(false, null, message);
    }
}
