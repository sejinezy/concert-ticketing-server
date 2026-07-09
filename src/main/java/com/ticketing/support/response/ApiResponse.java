package com.ticketing.support.response;

import com.ticketing.support.error.ErrorMessage;
import com.ticketing.support.error.ErrorType;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final ResultType resultType;
    private final T data;
    private final ErrorMessage error;

    public ApiResponse(ResultType resultType, T data, ErrorMessage error) {
        this.resultType = resultType;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse<Object> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static <S> ApiResponse<S> error(ErrorType errorType) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorType));
    }

    public static <S> ApiResponse<S> error(ErrorType errorType, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorType, errorData));
    }
}
