package sky.cicd.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    SUCCESS("S001", "요청이 성공했습니다아!"),
    CREATED("S002", "정상적으로 생성되었습니다"),
    UPDATED("S003", "정상적으로 수정되었습니다"),
    DELETED("S004", "정상적으로 삭제되었습니다");


    private final String code;
    private final String message;
}
