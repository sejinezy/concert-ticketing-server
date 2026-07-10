package com.ticketing.performance.application;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.presentation.dto.PerformanceCreateRequest;
import com.ticketing.performance.presentation.dto.PerformanceCreateResponse;
import com.ticketing.performance.presentation.dto.PerformanceResponse;
import com.ticketing.performance.presentation.dto.PerformanceUpdateRequest;
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
    public PerformanceCreateResponse create(PerformanceCreateRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new CoreException(ErrorType.EVENT_NOT_FOUND));

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new CoreException(ErrorType.VENUE_NOT_FOUND));

        Performance performance = Performance.create(
                event,
                venue,
                request.performanceStartAt(),
                request.bookingOpenAt(),
                request.bookingCloseAt()
        );

        Performance savedPerformance = performanceRepository.save(performance);

        return new PerformanceCreateResponse(savedPerformance.getId());
    }

    public PerformanceResponse getById(Long performanceId) {
        return performanceRepository.findResponseById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));
    }

    public List<PerformanceResponse> getByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new CoreException(ErrorType.EVENT_NOT_FOUND);
        }

        return performanceRepository.findResponsesByEventId(eventId);
    }

    @Transactional
    public PerformanceResponse update(Long performanceId, PerformanceUpdateRequest request) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new CoreException(ErrorType.VENUE_NOT_FOUND));

        performance.update(
                venue,
                request.performanceStartAt(),
                request.bookingOpenAt(),
                request.bookingCloseAt()
        );

        return performanceRepository.findResponseById(performance.getId())
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));
    }
}
