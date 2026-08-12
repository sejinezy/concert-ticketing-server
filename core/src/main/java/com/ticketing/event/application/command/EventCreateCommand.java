package com.ticketing.event.application.command;

public record EventCreateCommand(
        String title,
        String description
) {
}
