package com.ticketing.performance.presentation;

import com.ticketing.performance.application.PerformanceSeatService;
import com.ticketing.performance.application.result.PerformanceSeatCreateResult;
import com.ticketing.performance.application.result.PerformanceSeatResult;
import com.ticketing.performance.presentation.dto.PerformanceSeatCreateResponse;
import com.ticketing.performance.presentation.dto.PerformanceSeatResponse;
import com.ticketing.support.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PerformanceSeatController {

    private final PerformanceSeatService performanceSeatService;

    public PerformanceSeatController(PerformanceSeatService performanceSeatService) {
        this.performanceSeatService = performanceSeatService;
    }

    @PostMapping("/admin/performances/{performanceId}/seats")
    public ApiResponse<PerformanceSeatCreateResponse> create(
            @PathVariable Long performanceId
    ) {
        PerformanceSeatCreateResult result = performanceSeatService.create(performanceId);

        return ApiResponse.success(PerformanceSeatCreateResponse.from(result));
    }

    @GetMapping("/performances/{performanceId}/seats")
    public ApiResponse<List<PerformanceSeatResponse>> getPerformanceSeats(
            @PathVariable Long performanceId
    ) {
        List<PerformanceSeatResult> results = performanceSeatService.getPerformanceSeats(performanceId);

        List<PerformanceSeatResponse> response = results.stream()
                .map(PerformanceSeatResponse::from)
                .toList();

        return ApiResponse.success(response);
    }
}
