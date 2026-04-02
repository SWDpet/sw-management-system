package com.swmanager.system.exception;

import com.swmanager.system.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 일관된 형식으로 처리
 * 에러 페이지 템플릿이 없어도 작동하도록 개선
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(
            BusinessException e, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.error("BusinessException: {} - {}", e.getErrorCode(), e.getMessage());
        
        ErrorCode errorCode = e.getErrorCode();
        
        // API 요청인 경우 JSON 응답
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(errorCode.getStatus())
                    .body(ApiResponse.error(errorCode.getCode(), e.getMessage()));
        }
        
        // 일반 웹 요청인 경우 간단한 HTML 응답
        response.setStatus(errorCode.getStatus());
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(getErrorHtml(
            errorCode.getStatus(),
            errorCode.getCode(),
            e.getMessage()
        ));
        return null;
    }
    
    /**
     * ResourceNotFoundException 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(
            ResourceNotFoundException e, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.error("ResourceNotFoundException: {}", e.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
        }
        
        response.setStatus(404);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(getErrorHtml(404, "NOT_FOUND", e.getMessage()));
        return null;
    }
    
    /**
     * Spring Security AccessDeniedException 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(
            AccessDeniedException e, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.error("AccessDeniedException: {}", e.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("A003", "접근 권한이 없습니다."));
        }
        
        response.setStatus(403);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(getErrorHtml(403, "FORBIDDEN", "해당 페이지에 접근할 권한이 없습니다."));
        return null;
    }
    
    /**
     * Validation 예외 처리 (@Valid 검증 실패)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidationException(
            Exception e, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.error("ValidationException: {}", e.getMessage());
        
        String errorMessage = "입력값이 올바르지 않습니다.";
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("G001", errorMessage));
        }
        
        response.setStatus(400);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(getErrorHtml(400, "VALIDATION_ERROR", errorMessage));
        return null;
    }
    
    /**
     * 정적 리소스 없음 예외 (favicon.ico 등) - 무시
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException e) {
        // 로그만 남기고 무시 (favicon.ico 등)
        log.debug("Static resource not found: {}", e.getResourcePath());
    }
    
    /**
     * 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(
            NoHandlerFoundException e,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.error("NoHandlerFoundException: {}", e.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("R001", "요청한 페이지를 찾을 수 없습니다."));
        }
        
        response.setStatus(404);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(getErrorHtml(404, "NOT_FOUND", "요청한 페이지를 찾을 수 없습니다."));
        return null;
    }
    
    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(
            Exception e, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.error("Unexpected Exception: ", e);
        
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("S001", "서버 내부 오류가 발생했습니다."));
        }
        
        response.setStatus(500);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(getErrorHtml(500, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
        return null;
    }
    
    /**
     * API 요청 여부 판단
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        
        // /api/** 경로이거나 Accept 헤더가 application/json인 경우
        return uri.startsWith("/api/") || 
               (accept != null && accept.contains("application/json"));
    }
    
    /**
     * 간단한 에러 HTML 생성
     */
    private String getErrorHtml(int status, String errorCode, String message) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "    <meta charset='UTF-8'>" +
               "    <title>오류 " + status + "</title>" +
               "    <style>" +
               "        body { font-family: 'Malgun Gothic', sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f5f5f5; }" +
               "        .error-container { text-align: center; padding: 40px; background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 500px; }" +
               "        .error-code { font-size: 72px; font-weight: bold; color: #e53935; margin: 0; }" +
               "        .error-title { font-size: 24px; color: #333; margin: 20px 0 10px; }" +
               "        .error-message { font-size: 16px; color: #666; margin: 10px 0 30px; }" +
               "        .back-button { display: inline-block; padding: 12px 30px; background: #1976d2; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }" +
               "        .back-button:hover { background: #1565c0; }" +
               "    </style>" +
               "</head>" +
               "<body>" +
               "    <div class='error-container'>" +
               "        <div class='error-code'>" + status + "</div>" +
               "        <div class='error-title'>" + errorCode + "</div>" +
               "        <div class='error-message'>" + message + "</div>" +
               "        <a href='javascript:history.back()' class='back-button'>이전 페이지로</a>" +
               "        <a href='/' class='back-button' style='margin-left: 10px;'>메인으로</a>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }
}
