package com.ticketing.event.presentation;

import com.ticketing.event.application.EventService;
import com.ticketing.event.application.result.EventCreateResult;
import com.ticketing.event.application.result.EventResult;
import com.ticketing.event.presentation.dto.EventCreateRequest;
import com.ticketing.event.presentation.dto.EventCreateResponse;
import com.ticketing.event.presentation.dto.EventResponse;
import com.ticketing.event.presentation.dto.EventUpdateRequest;
import com.ticketing.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ApiResponse<EventCreateResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request
    ) {
        EventCreateResult result = eventService.createEvent(request.toCommand());

        return ApiResponse.success(EventCreateResponse.from(result));
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getEvents() {
        List<EventResponse> response =
                eventService.getEvents()
                        .stream()
                        .map(EventResponse::from)
                        .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> getEvent(
            @PathVariable Long eventId
    ) {
        EventResult result = eventService.getEvent(eventId);
        return ApiResponse.success(EventResponse.from(result));
    }

    @PatchMapping("/{eventId}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest request
    ) {
        EventResult result = eventService.updateEvent(request.toCommand(eventId));
        return ApiResponse.success(EventResponse.from(result));
    }

}
