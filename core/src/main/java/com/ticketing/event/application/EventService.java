package com.ticketing.event.application;

import com.ticketing.event.application.command.EventCreateCommand;
import com.ticketing.event.application.command.EventUpdateCommand;
import com.ticketing.event.application.result.EventCreateResult;
import com.ticketing.event.application.result.EventResult;
import com.ticketing.event.domain.Event;
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
    public EventCreateResult createEvent(EventCreateCommand command) {
        Event event = Event.create(
                command.title(),
                command.description()
        );

        Event savedEvent = eventRepository.save(event);

        return new EventCreateResult(savedEvent.getId());
    }

    public List<EventResult> getEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventResult::from)
                .toList();
    }

    public EventResult getEvent(Long eventId) {
        Event event = getEventEntity(eventId);

        return EventResult.from(event);
    }

    @Transactional
    public EventResult updateEvent(EventUpdateCommand command) {
        Event event = getEventEntity(command.eventId());
        event.update(
                command.title(),
                command.description()
        );
        return EventResult.from(event);
    }

    private Event getEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CoreException(ErrorType.EVENT_NOT_FOUND, eventId));
    }
}
