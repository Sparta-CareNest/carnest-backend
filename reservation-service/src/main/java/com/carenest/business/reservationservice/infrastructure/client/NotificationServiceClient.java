package com.carenest.business.reservationservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.request.NotificationCreateRequestDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.NotificationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/v1/notifications")
public interface NotificationServiceClient {

    @PostMapping("/reservation-created")
    ResponseDto<NotificationResponseDto> sendReservationCreatedNotification(
            @RequestBody NotificationCreateRequestDto request);

    @PostMapping("/payment-success")
    ResponseDto<NotificationResponseDto> sendPaymentSuccessNotification(
            @RequestBody NotificationCreateRequestDto request);

    @PostMapping("/settlement-completed")
    ResponseDto<NotificationResponseDto> sendSettlementCompletedNotification(
            @RequestBody NotificationCreateRequestDto request);
}