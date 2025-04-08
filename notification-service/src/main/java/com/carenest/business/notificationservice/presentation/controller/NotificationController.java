package com.carenest.business.notificationservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.application.service.NotificationService;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 1. 결제 성공 알림
    @PostMapping("/payment-success")
    public ResponseDto<NotificationResponseDto> sendPaymentSuccess(
            @RequestBody NotificationCreateRequestDto requestDto) {
        NotificationResponseDto responseDto = notificationService.createNotificationWithType(
                requestDto, NotificationType.PAYMENT_SUCCESS);
        log.info("결제 성공 알림 요청 들어옴");
        return ResponseDto.success("결제 성공 알림 전송 완료", null);
    }

    // 2. 예약 생성 알림
    @PostMapping("/reservation-created")
    public ResponseDto<Void> sendReservationCreated(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return ResponseDto.success("예약 생성 알림 전송 완료", null);
    }

    // 3. 정산 완료 알림
    @PostMapping("/settlement-complete")
    public ResponseDto<Void> sendSettlementComplete(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return ResponseDto.success("정산 완료 알림 전송 완료", null);
    }

    // 4. 알림 목록 조회
    @GetMapping
    public ResponseDto<List<NotificationResponseDto>> getNotifications(
            @RequestParam UUID receiverId) {
        return ResponseDto.success("알림 목록 조회 성공", List.of());
    }
}
