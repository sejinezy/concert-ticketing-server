package com.ticketing.event.presentation.dto;

import com.ticketing.event.application.result.EventResult;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EventResponse from(EventResult result) {
        return new EventResponse(
                result.id(),
                result.title(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
