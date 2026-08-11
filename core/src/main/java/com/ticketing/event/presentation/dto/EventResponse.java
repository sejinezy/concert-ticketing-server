package com.ticketing.event.presentation.dto;

import com.ticketing.event.domain.Event;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
