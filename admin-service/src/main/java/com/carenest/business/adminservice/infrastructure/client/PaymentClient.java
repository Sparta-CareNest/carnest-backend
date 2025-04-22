package com.carenest.business.adminservice.infrastructure.client;

import com.carenest.business.adminservice.application.dto.response.PaymentListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "payment-service", url = "http://localhost:9040")
public interface PaymentClient {
    @GetMapping("/api/v1/admin/payments")
    Page<PaymentListResponse> getPayments(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int size
    );
}
