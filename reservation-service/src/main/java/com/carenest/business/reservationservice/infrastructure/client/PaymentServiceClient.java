package com.carenest.business.reservationservice.infrastructure.client;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.request.PaymentCreateRequestDto;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.PaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "payment-service", path = "/api/v1")
public interface PaymentServiceClient {

    @PostMapping("/payments")
    ResponseDto<PaymentResponseDto> createPayment(@RequestBody PaymentCreateRequestDto request);

    @GetMapping("/payments/{paymentId}")
    ResponseDto<PaymentResponseDto> getPayment(@PathVariable("paymentId") UUID paymentId);

    @GetMapping("/reservations/{reservationId}/payment")
    ResponseDto<PaymentResponseDto> getPaymentByReservation(@PathVariable("reservationId") UUID reservationId);

    @PatchMapping("/payments/{paymentId}/cancel")
    ResponseDto<PaymentResponseDto> cancelPayment(@PathVariable("paymentId") UUID paymentId,
                                                  @RequestParam("cancelReason") String cancelReason);
}