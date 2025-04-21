package com.carenest.business.caregiverservice.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.carenest.business.caregiverservice.config.FeignConfig;
import com.carenest.business.caregiverservice.infrastructure.client.dto.reservation.ReservationResponse;
import com.carenest.business.common.response.ResponseDto;

@FeignClient(
	name = "reservation-service",
	configuration = FeignConfig.class
)
public interface ReservationClient {

	@GetMapping("/api/v1/internal/reservations/{reservationId}")
	ResponseDto<ReservationResponse> getReservationDetails(@PathVariable UUID reservationId);
}
