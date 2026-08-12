package com.ticketing.performance.presentation.dto;

import com.ticketing.performance.application.command.PerformanceUpdateCommand;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record PerformanceUpdateRequest(
        @NotNull(message = "공연장 Id는 필수입니다.") Long venueId,
        @NotNull(message = "공연 시작 시간은 필수입니다.") LocalDateTime performanceStartAt,
        @NotNull(message = "예매 오픈 시간은 필수입니다.") LocalDateTime bookingOpenAt,
        @NotNull(message = "예매 종료 시간은 필수입니다.") LocalDateTime bookingCloseAt
        ) {

        public PerformanceUpdateCommand toCommand(
                Long performanceId
        ) {
                return new PerformanceUpdateCommand(
                        performanceId,
                        venueId,
                        performanceStartAt,
                        bookingOpenAt,
                        bookingCloseAt
                );
        }
}
