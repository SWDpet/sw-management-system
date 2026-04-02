package com.swmanager.system.response;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * API 응답 표준 포맷
 * 
 * 성공 예시:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "조회 성공"
 * }
 * 
 * 실패 예시:
 * {
 *   "success": false,
 *   "errorCode": "R003",
 *   "message": "프로젝트를 찾을 수 없습니다."
 * }
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에서 제외
public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    
    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    
    /**
     * 성공 응답 (데이터 + 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }
    
    /**
     * 성공 응답 (메시지만)
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message, null);
    }
    
    /**
     * 실패 응답 (에러 코드 + 메시지)
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
    
    /**
     * 실패 응답 (메시지만)
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, "UNKNOWN");
    }
}
