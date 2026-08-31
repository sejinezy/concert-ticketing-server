package com.ticketing.support.error;

import com.ticketing.support.response.ApiResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ApiResponse<Object>> handleBulkheadFullException(
            BulkheadFullException exception
    ) {

        log.warn(
                "BulkheadFullException: external service concurrency limit exceeded. message={}",
                exception.getMessage()
        );

        return ResponseEntity
                .status(ErrorTypeHttpStatusMapper.resolve(ErrorType.PAYMENT_SERVICE_BUSY))
                .body(ApiResponse.error(ErrorType.PAYMENT_SERVICE_BUSY));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponse<Object>> handleRequestNotPermitted(
            RequestNotPermitted exception
    ) {
        log.warn(
                "RequestNotPermitted: payment rate limit exceeded. message={}",
                exception.getMessage()
        );

        return ResponseEntity
                .status(ErrorTypeHttpStatusMapper.resolve(
                        ErrorType.PAYMENT_RATE_LIMIT_EXCEEDED
                ))
                .body(ApiResponse.error(
                        ErrorType.PAYMENT_RATE_LIMIT_EXCEEDED
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("Exception: {}", exception.getMessage(), exception);

        return ResponseEntity
                .status(ErrorTypeHttpStatusMapper.resolve(ErrorType.DEFAULT_ERROR))
                .body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
    }
}
