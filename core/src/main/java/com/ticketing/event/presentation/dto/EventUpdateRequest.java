package com.ticketing.event.presentation.dto;

import jakarta.validation.constraints.Size;

public record EventUpdateRequest(

        @Size(max = 100, message = "이벤트 제목은 100자를 넘을 수 없습니다.")
        String title,

        String description
) {
}
