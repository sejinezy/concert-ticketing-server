package com.ticketing.support.error;

import org.springframework.http.HttpStatus;

public final class ErrorTypeHttpStatusMapper {
    private ErrorTypeHttpStatusMapper() {
    }

    public static HttpStatus resolve(ErrorType errorType) {
        return switch (errorType) {

            case INVALID_REQUEST,
                 INVALID_EVENT_TITLE,
                 INVALID_VENUE_NAME,
                 INVALID_VENUE_ADDRESS,
                 INVALID_VENUE_SEAT_VENUE,
                 INVALID_VENUE_SEAT_SECTION,
                 INVALID_VENUE_SEAT_ROW_LABEL,
                 INVALID_VENUE_SEAT_NO,
                 INVALID_PERFORMANCE_EVENT,
                 INVALID_PERFORMANCE_VENUE,
                 INVALID_PERFORMANCE_START_AT,
                 INVALID_PERFORMANCE_BOOKING_OPEN_AT,
                 INVALID_PERFORMANCE_BOOKING_CLOSE_AT,
                 INVALID_BOOKING_PERIOD,
                 INVALID_BOOKING_CLOSE_TIME,
                 INVALID_PERFORMANCE_SEAT_PERFORMANCE,
                 INVALID_PERFORMANCE_SEAT_VENUE_SEAT,
                 PERFORMANCE_SEAT_NOT_FOUND,
                 PERFORMANCE_SEAT_ALREADY_RESERVED,
                 INVALID_RESERVATION_QUEUE_ENTRY_ID,
                 INVALID_RESERVATION_PERFORMANCE_SEAT,
                 INVALID_RESERVATION_RESERVED_AT,
                 IDEMPOTENCY_KEY_REQUIRED,
                 INVALID_IDEMPOTENCY_KEY
                    -> HttpStatus.BAD_REQUEST;

            case NOT_FOUND_DATA,
                 EVENT_NOT_FOUND,
                 VENUE_NOT_FOUND,
                 PERFORMANCE_NOT_FOUND,
                 RESERVATION_NOT_FOUND
                    -> HttpStatus.NOT_FOUND;

            case RESERVATION_ACCESS_DENIED
                    -> HttpStatus.FORBIDDEN;

            case PERFORMANCE_SEATS_ALREADY_CREATED,
                 RESERVATION_NOT_CANCELLABLE,
                 IDEMPOTENCY_KEY_CONFLICT,
                 IDEMPOTENCY_REQUEST_IN_PROGRESS
                    -> HttpStatus.CONFLICT;

            case DEFAULT_ERROR,
                 PERFORMANCE_SEAT_RELEASE_FAILED,
                 INVALID_RESERVATION_STATUS_HISTORY_RESERVATION,
                 INVALID_RESERVATION_STATUS_HISTORY_CHANGED_AT,
                 INVALID_RESERVATION_STATUS_HISTORY_ACTOR_REFERENCE,
                 IDEMPOTENCY_RESULT_NOT_FOUND,
                 IDEMPOTENCY_REQUEST_NOT_FOUND,
                 IDEMPOTENCY_INVALID_STATE
                    -> HttpStatus.INTERNAL_SERVER_ERROR;

            case PAYMENT_SERVICE_BUSY
                    -> HttpStatus.SERVICE_UNAVAILABLE;

            case PAYMENT_RATE_LIMIT_EXCEEDED
                    -> HttpStatus.TOO_MANY_REQUESTS;
        };
    }
}
