package com.ticketing.reservation.presentation;

import com.ticketing.reservation.application.ReservationService;
import com.ticketing.reservation.application.result.ReservationResult;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/performance-seats/{performanceSeatId}/reservations")
    public ApiResponse<ReservationResponse> create(
            @RequestHeader(
                    value = IDEMPOTENCY_KEY_HEADER,
                    required = false
            )
            String idempotencyKey,
            @PathVariable Long performanceSeatId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationResult result = reservationService.create(
                request.toCommand(idempotencyKey, performanceSeatId)
        );

        return ApiResponse.success(ReservationResponse.from(result));
    }

    @GetMapping("/reservations/{reservationId}")
    public ApiResponse<ReservationResponse> get(
            @PathVariable Long reservationId
    ) {
        ReservationResult result = reservationService.get(reservationId);

        return ApiResponse.success(ReservationResponse.from(result));
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request
    ) {
        reservationService.cancel(request.toCommand(reservationId));

        return ApiResponse.success();
    }
}
