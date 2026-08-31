package com.ticketing.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;

@Getter
public enum ErrorType {
    DEFAULT_ERROR(
            ErrorCode.E500,
            "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            LogLevel.ERROR
    ),

    INVALID_REQUEST(
            ErrorCode.E400,
            "요청이 올바르지 않습니다.",
            LogLevel.INFO
    ),

    NOT_FOUND_DATA(
            ErrorCode.E404,
            "해당 데이터를 찾을 수 없습니다.",
            LogLevel.ERROR
    ),

    // 이벤트
    EVENT_NOT_FOUND(
            ErrorCode.E1000,
            "존재하지 않는 이벤트입니다.",
            LogLevel.INFO
    ),

    INVALID_EVENT_TITLE(
            ErrorCode.E1001,
            "이벤트 제목은 필수입니다.",
            LogLevel.INFO
    ),

    // 공연장
    INVALID_VENUE_NAME(
            ErrorCode.E2000,
            "공연장 이름은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_ADDRESS(
            ErrorCode.E2001,
            "공연장 주소는 필수입니다.",
            LogLevel.INFO
    ),

    VENUE_NOT_FOUND(
            ErrorCode.E2002,
            "존재하지 않는 공연장입니다.",
            LogLevel.INFO
    ),

    // 공연장 좌석
    INVALID_VENUE_SEAT_VENUE(
            ErrorCode.E3000,
            "좌석은 반드시 공연장에 속해야 합니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_SEAT_SECTION(
            ErrorCode.E3001,
            "구역은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_SEAT_ROW_LABEL(
            ErrorCode.E3002,
            "열은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_VENUE_SEAT_NO(
            ErrorCode.E3003,
            "좌석 번호는 필수입니다.",
            LogLevel.INFO
    ),

    // 회차
    INVALID_PERFORMANCE_EVENT(
            ErrorCode.E4000,
            "이벤트는 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_PERFORMANCE_VENUE(
            ErrorCode.E4001,
            "공연장은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_PERFORMANCE_START_AT(
            ErrorCode.E4002,
            "공연 시작 시간은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_PERFORMANCE_BOOKING_OPEN_AT(
            ErrorCode.E4003,
            "예매 오픈 시간은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_PERFORMANCE_BOOKING_CLOSE_AT(
            ErrorCode.E4004,
            "예매 종료 시간은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_BOOKING_PERIOD(
            ErrorCode.E4005,
            "예매 오픈 시간은 예매 종료 시간보다 빨라야 합니다.",
            LogLevel.INFO
    ),

    INVALID_BOOKING_CLOSE_TIME(
            ErrorCode.E4006,
            "예매 종료 시간은 공연 시작 시간보다 최소 24시간 전이어야 합니다.",
            LogLevel.INFO
    ),

    PERFORMANCE_NOT_FOUND(
            ErrorCode.E4007,
            "존재하지 않는 회차입니다.",
            LogLevel.INFO
    ),

    // 회차 좌석
    INVALID_PERFORMANCE_SEAT_PERFORMANCE(
            ErrorCode.E5000,
            "공연 회차는 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_PERFORMANCE_SEAT_VENUE_SEAT(
            ErrorCode.E5001,
            "공연장 좌석은 필수입니다.",
            LogLevel.INFO
    ),

    PERFORMANCE_SEATS_ALREADY_CREATED(
            ErrorCode.E5002,
            "이미 좌석이 생성된 공연 회차입니다.",
            LogLevel.INFO
    ),

    PERFORMANCE_SEAT_NOT_FOUND(
            ErrorCode.E5003,
            "해당 공연장에 등록된 좌석이 없습니다.",
            LogLevel.INFO
    ),

    PERFORMANCE_SEAT_ALREADY_RESERVED(
            ErrorCode.E5004,
            "이미 예약된 좌석입니다",
            LogLevel.INFO
    ),

    PERFORMANCE_SEAT_RELEASE_FAILED(
            ErrorCode.E5005,
            "예약 좌석 복구에 실패했습니다.",
            LogLevel.ERROR
    ),

    // 예약
    INVALID_RESERVATION_QUEUE_ENTRY_ID(
            ErrorCode.E6000,
            "대기열 참여 식별자는 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_RESERVATION_PERFORMANCE_SEAT(
            ErrorCode.E6001,
            "예약 좌석은 필수입니다.",
            LogLevel.INFO
    ),

    INVALID_RESERVATION_RESERVED_AT(
            ErrorCode.E6002,
            "예약 시각은 필수입니다.",
            LogLevel.INFO
    ),

    RESERVATION_NOT_FOUND(
            ErrorCode.E6003,
            "존재하지 않는 예약입니다.",
            LogLevel.INFO
    ),

    RESERVATION_ACCESS_DENIED(
            ErrorCode.E6004,
            "해당 예약을 취소할 권한이 없습니다.",
            LogLevel.INFO
    ),

    RESERVATION_NOT_CANCELLABLE(
            ErrorCode.E6005,
            "취소할 수 없는 예약 상태입니다.",
            LogLevel.INFO
    ),

    // 예약 변경 이력
    INVALID_RESERVATION_STATUS_HISTORY_RESERVATION(
            ErrorCode.E7000,
            "예약 상태 변경 이력에는 예약 정보가 필요합니다.",
            LogLevel.ERROR
    ),
    INVALID_RESERVATION_STATUS_HISTORY_CHANGED_AT(
            ErrorCode.E7001,
            "예약 상태 변경 이력에는 변경 시각이 필요합니다.",
            LogLevel.ERROR
    ),
    INVALID_RESERVATION_STATUS_HISTORY_ACTOR_REFERENCE(
            ErrorCode.E7002,
            "예약 상태 변경 주체 식별자가 누락되었습니다.",
            LogLevel.ERROR
    ),

    //idempotency
    IDEMPOTENCY_KEY_REQUIRED(
            ErrorCode.E8000,
            "멱등성 키는 필수입니다.",
            LogLevel.INFO
    ),
    INVALID_IDEMPOTENCY_KEY(
            ErrorCode.E8001,
            "멱등성 키는 UUID 형식이어야 합니다.",
            LogLevel.INFO
    ),
    IDEMPOTENCY_KEY_CONFLICT(
            ErrorCode.E8002,
            "동일한 멱등성 키가 다른 요청에 사용되었습니다.",
            LogLevel.INFO
    ),
    IDEMPOTENCY_REQUEST_IN_PROGRESS(
            ErrorCode.E8003,
            "동일한 요청이 처리 중입니다.",
            LogLevel.INFO
    ),
    IDEMPOTENCY_RESULT_NOT_FOUND(
            ErrorCode.E8004,
            "완료된 멱등성 요청의 처리 결과를 찾을 수 없습니다.",
            LogLevel.ERROR
    ),
    IDEMPOTENCY_REQUEST_NOT_FOUND(
            ErrorCode.E8005,
            "멱등성 요청 정보를 찾을 수 없습니다.",
            LogLevel.ERROR
    ),
    IDEMPOTENCY_INVALID_STATE(
            ErrorCode.E8006,
            "멱등성 요청 상태가 올바르지 않습니다.",
            LogLevel.ERROR
    ),

    // 외부 서비스 - Payment
    PAYMENT_SERVICE_BUSY(
            ErrorCode.E9000,
            "결제 서비스가 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요.",
            LogLevel.WARN
    ),

    PAYMENT_RATE_LIMIT_EXCEEDED(
            ErrorCode.E9001,
            "결제 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
            LogLevel.WARN
    );



    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(ErrorCode code, String message, LogLevel logLevel) {
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }
    }
