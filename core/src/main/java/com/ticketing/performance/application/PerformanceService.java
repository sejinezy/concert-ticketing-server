package com.ticketing.performance.application;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.performance.application.command.PerformanceCreateCommand;
import com.ticketing.performance.application.command.PerformanceUpdateCommand;
import com.ticketing.performance.application.result.PerformanceCreateResult;
import com.ticketing.performance.application.result.PerformanceResult;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.repository.PerformanceRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.repository.VenueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public PerformanceService(PerformanceRepository performanceRepository, EventRepository eventRepository,
                              VenueRepository venueRepository) {
        this.performanceRepository = performanceRepository;
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    @Transactional
    public PerformanceCreateResult create(PerformanceCreateCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new CoreException(ErrorType.EVENT_NOT_FOUND));

        Venue venue = venueRepository.findById(command.venueId())
                .orElseThrow(() -> new CoreException(ErrorType.VENUE_NOT_FOUND));

        Performance performance = Performance.create(
                event,
                venue,
                command.performanceStartAt(),
                command.bookingOpenAt(),
                command.bookingCloseAt()
        );

        Performance savedPerformance = performanceRepository.save(performance);

        return new PerformanceCreateResult(savedPerformance.getId());
    }

    public PerformanceResult getById(Long performanceId) {
        return performanceRepository.findResponseById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));
    }

    public List<PerformanceResult> getByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new CoreException(ErrorType.EVENT_NOT_FOUND);
        }

        return performanceRepository.findResponsesByEventId(eventId);
    }

    @Transactional
    public PerformanceResult update(PerformanceUpdateCommand command) {
        Performance performance = performanceRepository.findById(command.performanceId())
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));

        Venue venue = venueRepository.findById(command.venueId())
                .orElseThrow(() -> new CoreException(ErrorType.VENUE_NOT_FOUND));

        performance.update(
                venue,
                command.performanceStartAt(),
                command.bookingOpenAt(),
                command.bookingCloseAt()
        );

        return performanceRepository.findResponseById(performance.getId())
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));
    }
}
