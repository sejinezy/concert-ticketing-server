package com.ticketing.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    DEFAULT_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.E500,
            "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            LogLevel.ERROR
    ),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E400,
            "요청이 올바르지 않습니다.",
            LogLevel.INFO
    ),

    NOT_FOUND_DATA(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "해당 데이터를 찾을 수 없습니다.",
            LogLevel.ERROR
    ),

    // 이벤트
    EVENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E1000,
            "존재하지 않는 이벤트입니다.",
            LogLevel.INFO
    ),

    INVALID_EVENT_TITLE(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E1001,
            "이벤트 제목은 필수입니다.",
            LogLevel.INFO
    ),

    // 공연장
    INVALID_VENUE_NAME(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E2000,
            "공연장 이름은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_ADDRESS(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E2001,
            "공연장 주소는 필수입니다.",
            LogLevel.INFO
    ),

    // 공연장 좌석
    INVALID_VENUE_SEAT_VENUE(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E3000,
            "좌석은 반드시 공연장에 속해야 합니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_SEAT_SECTION(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E3001,
            "구역은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_SEAT_ROW_LABEL(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E3002,
            "열은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_SEAT_NO(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E3003,
            "좌석 번호는 필수입니다.",
            LogLevel.INFO
    );

    private final HttpStatus status;
    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(HttpStatus status, ErrorCode code, String message, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }
}
