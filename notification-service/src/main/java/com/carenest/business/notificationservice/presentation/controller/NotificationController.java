package com.carenest.business.notificationservice.presentation.controller;

import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    // 1. 결제 성공 알림
    @PostMapping("/payment-success")
    public ApiResponse<Void> sendPaymentSuccess(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return ApiResponse.success("결제 성공 알림 전송 완료", null);
    }

    // 2. 예약 생성 알림
    @PostMapping("/reservation-created")
    public ApiResponse<Void> sendReservationCreated(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return ApiResponse.success("예약 생성 알림 전송 완료", null);
    }

    // 3. 정산 완료 알림
    @PostMapping("/settlement-complete")
    public ApiResponse<Void> sendSettlementComplete(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return ApiResponse.success("정산 완료 알림 전송 완료", null);
    }

    // 4. 알림 목록 조회
    @GetMapping
    public ApiResponse<List<NotificationResponseDto>> getNotifications(
            @RequestParam UUID receiverId) {
        return ApiResponse.success("알림 목록 조회 성공", List.of());
    }
}
