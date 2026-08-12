package com.ticketing.event.presentation.dto;

import com.ticketing.event.application.command.EventUpdateCommand;
import jakarta.validation.constraints.Size;

public record EventUpdateRequest(

        @Size(max = 100, message = "이벤트 제목은 100자를 넘을 수 없습니다.")
        String title,

        String description
) {

        public EventUpdateCommand toCommand(Long eventId) {
                return new EventUpdateCommand(
                        eventId,
                        title,
                        description
                );
        }
}
