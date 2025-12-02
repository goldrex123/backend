package sky.cicd.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import sky.cicd.common.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode {

    VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "V001", "잘못된 요청입니다"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "요청하신 데이터가 없습니다"),
    REQUEST_BODY_NOT_READABLE(HttpStatus.BAD_REQUEST, "E002", "잘못된 REQUEST BODY 요청 입니다"),
    REQUEST_METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "E003", "잘못된 요청 메소드 입니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "잘못된 API 경로 요청입니다"),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "E005", "잘못된 요청 파라미터 타입입니다"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "E006", "데이터 무결성 오류 입니다"),
    ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "E007", "요청을 수행할 수 없는 상태입니다"),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "E008", "잘못된 요청 인자입니다"),
    SERVER_SIDE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "서버 측 오류 입니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
