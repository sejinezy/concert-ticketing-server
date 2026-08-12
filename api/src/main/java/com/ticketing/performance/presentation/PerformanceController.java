package com.ticketing.performance.presentation;

import com.ticketing.performance.application.PerformanceService;
import com.ticketing.performance.application.result.PerformanceCreateResult;
import com.ticketing.performance.application.result.PerformanceResult;
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
        PerformanceCreateResult result =
                performanceService.create(
                        request.toCommand()
                );

        return ApiResponse.success(
                PerformanceCreateResponse.from(result)
        );
    }

    @GetMapping("/api/admin/performances/{performanceId}")
    public ApiResponse<PerformanceResponse> getById(
            @PathVariable Long performanceId
    ) {
        PerformanceResult result = performanceService.getById(performanceId);

        return ApiResponse.success(PerformanceResponse.from(result));
    }

    @GetMapping("/api/events/{eventId}/performances")
    public ApiResponse<List<PerformanceResponse>> getByEventId(
            @PathVariable Long eventId
    ) {
        List<PerformanceResponse> response =
                performanceService
                        .getByEventId(eventId)
                        .stream()
                        .map(PerformanceResponse::from)
                        .toList();

        return ApiResponse.success(response);
    }

    @PutMapping("/api/admin/performances/{performanceId}")
    public ApiResponse<PerformanceResponse> update(
            @PathVariable Long performanceId,
            @Valid @RequestBody PerformanceUpdateRequest request
    ) {
        PerformanceResult result = performanceService.update(request.toCommand(performanceId));

        return ApiResponse.success(PerformanceResponse.from(result));
    }

}
