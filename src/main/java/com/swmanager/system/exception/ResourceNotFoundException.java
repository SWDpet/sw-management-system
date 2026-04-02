package com.swmanager.system.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class ResourceNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("%s를 찾을 수 없습니다. (ID: %s)", resourceName, identifier));
    }
    
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
