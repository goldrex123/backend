package sky.cicd.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
