package com.swmanager.system.exception;

/**
 * 잘못된 입력값이 들어왔을 때 발생하는 예외
 */
public class InvalidInputException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String fieldName, String reason) {
        super(ErrorCode.INVALID_INPUT_VALUE,
              String.format("%s이(가) 올바르지 않습니다. (%s)", fieldName, reason));
    }
    
    public InvalidInputException(ErrorCode errorCode) {
        super(errorCode);
    }
}
