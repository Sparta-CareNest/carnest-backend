package com.carenest.business.paymentservice.infrastructure.client;

import com.carenest.business.paymentservice.infrastructure.client.dto.request.PaySoApprovalRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.PaySoCancelRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.request.PaySoPrepareRequestDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.PaySoApprovalResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.PaySoCancelResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.PaySoPrepareResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payso-service", url = "${payso.api.url}")
public interface PaymentGatewayClient {

    @PostMapping("/payments/prepare")
    PaySoPrepareResponseDto preparePayment(@RequestBody PaySoPrepareRequestDto request);

    @PostMapping("/payments/approve")
    PaySoApprovalResponseDto approvePayment(@RequestBody PaySoApprovalRequestDto request);

    @PostMapping("/payments/cancel")
    PaySoCancelResponseDto cancelPayment(@RequestBody PaySoCancelRequestDto request);

    @GetMapping("/payments/status/{paymentKey}")
    PaySoApprovalResponseDto getPaymentStatus(@PathVariable("paymentKey") String paymentKey);
}