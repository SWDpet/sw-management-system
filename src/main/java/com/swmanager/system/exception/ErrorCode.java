package com.swmanager.system.exception;

/**
 * 에러 코드 정의
 * 각 에러에 대한 HTTP 상태 코드와 메시지 관리
 */
public enum ErrorCode {
    
    // 일반 오류 (400번대)
    INVALID_INPUT_VALUE(400, "G001", "잘못된 입력 값입니다."),
    MISSING_REQUIRED_PARAMETER(400, "G002", "필수 파라미터가 누락되었습니다."),
    INVALID_TYPE_VALUE(400, "G003", "잘못된 타입입니다."),
    
    // 인증/인가 오류 (401, 403)
    UNAUTHORIZED(401, "A001", "인증이 필요합니다."),
    INVALID_CREDENTIALS(401, "A002", "아이디 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(403, "A003", "접근 권한이 없습니다."),
    ACCOUNT_NOT_APPROVED(403, "A004", "승인 대기 중인 계정입니다."),
    INSUFFICIENT_PERMISSION(403, "A005", "해당 기능에 대한 권한이 부족합니다."),
    
    // 리소스 오류 (404)
    RESOURCE_NOT_FOUND(404, "R001", "요청한 리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(404, "R002", "사용자를 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(404, "R003", "프로젝트를 찾을 수 없습니다."),
    INFRA_NOT_FOUND(404, "R004", "인프라 정보를 찾을 수 없습니다."),
    PERSON_NOT_FOUND(404, "R005", "담당자 정보를 찾을 수 없습니다."),
    
    // 중복 오류 (409)
    DUPLICATE_RESOURCE(409, "D001", "이미 존재하는 리소스입니다."),
    DUPLICATE_USER_ID(409, "D002", "이미 사용 중인 아이디입니다."),
    DUPLICATE_PROJECT(409, "D003", "해당 연도와 지역에 이미 동일한 시스템이 등록되어 있습니다."),
    
    // 서버 오류 (500번대)
    INTERNAL_SERVER_ERROR(500, "S001", "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(500, "S002", "데이터베이스 오류가 발생했습니다."),
    FILE_UPLOAD_ERROR(500, "S003", "파일 업로드 중 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(500, "S004", "외부 API 호출 중 오류가 발생했습니다.");
    
    private final int status;
    private final String code;
    private final String message;
    
    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
