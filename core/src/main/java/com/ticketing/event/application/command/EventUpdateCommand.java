package com.ticketing.event.application.command;

public record EventUpdateCommand(
        Long eventId,
        String title,
        String description
) {
}
