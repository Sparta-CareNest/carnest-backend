package com.carenest.business.paymentservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.ReservationDetailsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "reservation-service", path = "/api/v1/internal")
public interface ReservationInternalClient {

    @GetMapping("/reservations/{reservationId}")
    ResponseDto<ReservationDetailsResponseDto> getReservationInfo(@PathVariable UUID reservationId);

    default ReservationDetailsResponseDto getReservationDetails(UUID reservationId) {
        ResponseDto<ReservationDetailsResponseDto> response = getReservationInfo(reservationId);
        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return null;
    }
}