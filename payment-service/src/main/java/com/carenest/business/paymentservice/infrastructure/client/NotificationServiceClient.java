package com.carenest.business.paymentservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.NotificationCreateRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.NotificationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/v1/notifications")
public interface NotificationServiceClient {

    @PostMapping("/payment-success")
    ResponseDto<NotificationResponseDto> sendPaymentSuccessNotification(
            @RequestBody NotificationCreateRequestDto request);

    @PostMapping("/payment-canceled")
    ResponseDto<NotificationResponseDto> sendPaymentCanceledNotification(
            @RequestBody NotificationCreateRequestDto request);

    @PostMapping("/payment-refunded")
    ResponseDto<NotificationResponseDto> sendPaymentRefundedNotification(
            @RequestBody NotificationCreateRequestDto request);
}