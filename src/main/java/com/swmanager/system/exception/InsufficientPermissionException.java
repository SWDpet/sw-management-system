package com.swmanager.system.exception;

/**
 * 권한이 부족할 때 발생하는 예외
 */
public class InsufficientPermissionException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public InsufficientPermissionException(String action) {
        super(ErrorCode.INSUFFICIENT_PERMISSION,
              String.format("%s 권한이 없습니다.", action));
    }
    
    public InsufficientPermissionException() {
        super(ErrorCode.INSUFFICIENT_PERMISSION);
    }
}
