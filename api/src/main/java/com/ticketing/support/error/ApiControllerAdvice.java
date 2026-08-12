package com.ticketing.support.error;

import com.ticketing.support.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<Object>> handleCoreException(CoreException exception) {
        ErrorType errorType = exception.getErrorType();

        if (errorType.getLogLevel() == LogLevel.ERROR) {
            log.error("CoreException : {}", exception.getMessage(), exception);

        } else if (errorType.getLogLevel() == LogLevel.WARN) {
            log.warn("CoreException: {}", exception.getMessage(), exception);
        } else {
            log.info("CoreException : {}", exception.getMessage(), exception);
        }

        return ResponseEntity
                .status(ErrorTypeHttpStatusMapper.resolve(errorType))
                .body(ApiResponse.error(errorType, exception.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(ErrorType.INVALID_REQUEST.getMessage());

        return ResponseEntity
                .status(ErrorTypeHttpStatusMapper.resolve(ErrorType.INVALID_REQUEST))
                .body(ApiResponse.error(ErrorType.INVALID_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("Exception: {}", exception.getMessage(), exception);

        return ResponseEntity
                .status(ErrorTypeHttpStatusMapper.resolve(ErrorType.DEFAULT_ERROR))
                .body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
    }
}
