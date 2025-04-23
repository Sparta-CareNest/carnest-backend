package com.carenest.business.caregiverservice.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.carenest.business.caregiverservice.config.FeignConfig;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationAcceptRequest;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationRejectRequest;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationResponse;
import com.carenest.business.common.response.ResponseDto;

import jakarta.validation.Valid;

@FeignClient(name = "reservation-service", configuration = FeignConfig.class)
public interface ReservationClient {

	@GetMapping("/api/v1/internal/reservations/{reservationId}")
	ResponseDto<ReservationResponse> getReservationDetails(@PathVariable UUID reservationId);

	@PostMapping("/api/v1/internal/reservations/{reservationId}/accept")
	ResponseDto<ReservationResponse> acceptReservation(@PathVariable UUID reservationId,
		@RequestBody @Valid ReservationAcceptRequest request);

	@PostMapping("/api/v1/internal/reservations/{reservationId}/reject")
	ResponseDto<ReservationResponse> rejectReservation(@PathVariable UUID reservationId,
		@RequestBody @Valid ReservationRejectRequest request);
}

