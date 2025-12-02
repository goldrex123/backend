package sky.cicd.common.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import sky.cicd.common.api.ApiResponse;
import sky.cicd.common.api.ErrorCode;
import sky.cicd.common.exception.CustomException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("handle EntityNotFoundException - {}", ex.getMessage());

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.ENTITY_NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("handle MethodArgumentNotValidException - {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response = ApiResponse.fail(ErrorCode.VALIDATION_FAIL, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.error("handle HandlerMethodValidationException - {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();

        ex.getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });

        ApiResponse<Map<String, String>> response = ApiResponse.fail(ErrorCode.VALIDATION_FAIL, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("handle HttpMessageNotReadableException - {}", ex.getMessage());

        Throwable cause = ex.getCause();
        String message = "잘못된 Request Body 데이터 입니다";

        if (cause instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();
            String fieldName = invalidFormatException.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .reduce((prev, curr) -> curr)
                    .orElse("알 수 없는 필드");

            message = String.format("%s 필드는 %s 타입이어야 합니다.", fieldName, targetType.getSimpleName());

            if (targetType.isEnum()) {
                Object[] enumConstants = targetType.getEnumConstants();
                String allowed = Arrays.stream(enumConstants)
                        .map(Object::toString)
                        .collect(Collectors.joining(","));

                message += " 허용 값: [" + allowed + "]";
            }
        }

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.REQUEST_BODY_NOT_READABLE, message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("handle HttpRequestMethodNotSupportedException - {}", ex.getMessage());

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.REQUEST_METHOD_NOT_SUPPORTED);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("handle MethodArgumentTypeMismatchException - {}", ex.getMessage());

        String name = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음";

        String message = String.format("%s 파라미터는 %s 타입이어야 합니다.", name, requiredType);

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.TYPE_MISMATCH, message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("handle DataIntegrityViolationException - {}", ex.getMessage());

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.DATA_INTEGRITY_VIOLATION);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("handle IllegalStateException - {}", ex.getMessage());

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.ILLEGAL_STATE);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("handle IllegalArgumentException - {}", ex.getMessage());

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.ILLEGAL_ARGUMENT);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("handle NoHandlerFoundException - {}", ex.getMessage());

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.NOT_FOUND);
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {
        log.error("handle Exception - {}", ex.getMessage(), ex);

        ApiResponse<String> response = ApiResponse.fail(ErrorCode.SERVER_SIDE_EXCEPTION);
        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException e) {
        log.error("handle CustomException - {}", e.getMessage(), e);

        ApiResponse<String> response = ApiResponse.fail(e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }

}
