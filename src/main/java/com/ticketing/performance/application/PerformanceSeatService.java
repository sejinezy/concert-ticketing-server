package com.ticketing.performance.application;

import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.presentation.dto.PerformanceSeatCreateResponse;
import com.ticketing.performance.presentation.dto.PerformanceSeatResponse;
import com.ticketing.performance.repository.PerformanceRepository;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.venue.domain.VenueSeat;
import com.ticketing.venue.repository.VenueSeatRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerformanceSeatService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final VenueSeatRepository venueSeatRepository;

    public PerformanceSeatService(PerformanceRepository performanceRepository,
                                  PerformanceSeatRepository performanceSeatRepository,
                                  VenueSeatRepository venueSeatRepository) {
        this.performanceRepository = performanceRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.venueSeatRepository = venueSeatRepository;
    }

    @Transactional
    public PerformanceSeatCreateResponse create(Long performanceId) {
        Performance performance = getPerformance(performanceId);

        validatePerformanceSeatsNotCreated(performanceId);

        Long venueId = performance.getVenue().getId();

        List<VenueSeat> venueSeats =
                venueSeatRepository.findAllByVenue_IdOrderByIdAsc(venueId);

        validateVenueSeatsExist(venueSeats);

        List<PerformanceSeat> performanceSeats = venueSeats.stream()
                .map(venueSeat -> PerformanceSeat.create(
                        performance,
                        venueSeat
                ))
                .toList();

        savePerformanceSeats(performanceSeats);

        return new PerformanceSeatCreateResponse(performanceId, (long) performanceSeats.size());

    }

    public List<PerformanceSeatResponse> getPerformanceSeats(Long performanceId) {
        validatePerformanceExists(performanceId);

        return performanceSeatRepository.findAllByPerformanceId(performanceId);
    }

    private void validatePerformanceExists(Long performanceId) {
        if (!performanceRepository.existsById(performanceId)) {
            throw new CoreException(ErrorType.PERFORMANCE_NOT_FOUND);
        }
    }

    private Performance getPerformance(Long performanceId) {
        return performanceRepository.findById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.PERFORMANCE_NOT_FOUND));
    }

    private void validatePerformanceSeatsNotCreated(Long performanceId) {
        boolean alreadyCreated = performanceSeatRepository.existsByPerformance_Id(performanceId);

        if (alreadyCreated) {
            throw new CoreException(ErrorType.PERFORMANCE_SEATS_ALREADY_CREATED);
        }
    }

    private void validateVenueSeatsExist(List<VenueSeat> venueSeats) {
        if (venueSeats.isEmpty()) {
            throw new CoreException(ErrorType.VENUE_SEATS_NOT_FOUND);
        }
    }

    private void savePerformanceSeats(List<PerformanceSeat> performanceSeats) {

        try {
            performanceSeatRepository.saveAllAndFlush(performanceSeats);
        } catch (DataIntegrityViolationException exception) {

            throw new CoreException(ErrorType.PERFORMANCE_SEATS_ALREADY_CREATED);
        }
    }
}
