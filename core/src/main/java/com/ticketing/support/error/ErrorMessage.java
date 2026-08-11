package com.ticketing.support.error;

public record ErrorMessage(
        String code,
        String message,
        Object data

){
    public ErrorMessage(ErrorType errorType) {
        this(errorType, null);
    }

    public ErrorMessage(ErrorType errorType, Object data) {
        this(
                errorType.getCode().name(),
                errorType.getMessage(),
                data
        );
    }
}
