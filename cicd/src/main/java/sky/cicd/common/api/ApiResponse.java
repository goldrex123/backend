package sky.cicd.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.cicd.common.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(SuccessCode code, T data) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode code, T data) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode code){
        return new ApiResponse<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(BaseErrorCode errorCode, T data) {
        String code = errorCode instanceof ErrorCode ? ((ErrorCode) errorCode).getCode() : "ERROR";
        return new ApiResponse<>(code, errorCode.getMessage(), data);
    }
}
