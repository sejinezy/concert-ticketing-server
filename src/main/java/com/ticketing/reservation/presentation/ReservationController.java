package com.ticketing.reservation.presentation;

import com.ticketing.reservation.application.ReservationService;
import com.ticketing.reservation.presentation.dto.ReservationCancelRequest;
import com.ticketing.reservation.presentation.dto.ReservationCreateRequest;
import com.ticketing.reservation.presentation.dto.ReservationResponse;
import com.ticketing.support.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/performance-seats/{performanceSeatId}/reservations")
    public ApiResponse<ReservationResponse> create(
            @PathVariable Long performanceSeatId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationResponse response = reservationService.create(performanceSeatId, request);

        return ApiResponse.success(response);
    }

    @GetMapping("/reservations/{reservationId}")
    public ApiResponse<ReservationResponse> get(
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(reservationService.get(reservationId));
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request
    ) {
        reservationService.cancel(reservationId, request);

        return ApiResponse.success();
    }
}
