package com.ticketing.performance.presentation;

import com.ticketing.performance.application.PerformanceService;
import com.ticketing.performance.presentation.dto.PerformanceCreateRequest;
import com.ticketing.performance.presentation.dto.PerformanceCreateResponse;
import com.ticketing.performance.presentation.dto.PerformanceResponse;
import com.ticketing.performance.presentation.dto.PerformanceUpdateRequest;
import com.ticketing.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping("/api/admin/performances")
    public ApiResponse<PerformanceCreateResponse> create(
            @Valid @RequestBody PerformanceCreateRequest request
    ) {
        PerformanceCreateResponse response = performanceService.create(request);

        return ApiResponse.success(response);
    }

    @GetMapping("/api/admin/performances/{performanceId}")
    public ApiResponse<PerformanceResponse> getById(
            @PathVariable Long performanceId
    ) {
        PerformanceResponse response = performanceService.getById(performanceId);

        return ApiResponse.success(response);
    }

    @GetMapping("/api/events/{eventId}/performances")
    public ApiResponse<List<PerformanceResponse>> getByEventId(
            @PathVariable Long eventId
    ) {
        List<PerformanceResponse> response = performanceService.getByEventId(eventId);

        return ApiResponse.success(response);
    }

    @PutMapping("/api/admin/performances/{performanceId}")
    public ApiResponse<PerformanceResponse> update(
            @PathVariable Long performanceId,
            @Valid @RequestBody PerformanceUpdateRequest request
    ) {
        PerformanceResponse response = performanceService.update(performanceId, request);

        return ApiResponse.success(response);
    }
}
