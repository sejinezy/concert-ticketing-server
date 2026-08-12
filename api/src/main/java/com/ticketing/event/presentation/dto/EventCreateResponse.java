package com.ticketing.event.presentation.dto;

import com.ticketing.event.application.result.EventCreateResult;

public record EventCreateResponse(
        Long id
) {

    public static EventCreateResponse from(EventCreateResult result) {
        return new EventCreateResponse(result.id());
    }
}
