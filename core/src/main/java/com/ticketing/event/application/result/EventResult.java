package com.ticketing.event.application.result;

import com.ticketing.event.domain.Event;
import java.time.LocalDateTime;

public record EventResult(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EventResult from(Event event) {
        return new EventResult(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
