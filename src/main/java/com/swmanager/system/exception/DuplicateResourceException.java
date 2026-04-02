package com.swmanager.system.exception;

/**
 * 중복된 리소스가 존재할 때 발생하는 예외
 */
public class DuplicateResourceException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(ErrorCode.DUPLICATE_RESOURCE,
              String.format("이미 존재하는 %s입니다. (%s: %s)", resourceName, field, value));
    }
    
    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
