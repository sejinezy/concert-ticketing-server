package com.ticketing.event.application;

import com.ticketing.event.domain.Event;
import com.ticketing.event.presentation.dto.EventCreateRequest;
import com.ticketing.event.presentation.dto.EventCreateResponse;
import com.ticketing.event.presentation.dto.EventResponse;
import com.ticketing.event.presentation.dto.EventUpdateRequest;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventCreateResponse createEvent(EventCreateRequest request) {
        Event event = Event.create(
                request.title(),
                request.description()
        );

        Event savedEvent = eventRepository.save(event);

        return new EventCreateResponse(savedEvent.getId());
    }

    public List<EventResponse> getEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getEvent(Long eventId) {
        Event event = getEventEntity(eventId);

        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventUpdateRequest request) {
        Event event = getEventEntity(eventId);

        event.update(
                request.title(),
                request.description()
        );
        return EventResponse.from(event);
    }

    private Event getEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CoreException(ErrorType.EVENT_NOT_FOUND, eventId));
    }
}
