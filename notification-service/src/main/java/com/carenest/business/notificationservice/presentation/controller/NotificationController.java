package com.carenest.business.notificationservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.application.service.NotificationService;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import com.carenest.business.notificationservice.infrastructure.util.AuthValidationUtil;
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
        return sendNotification(requestDto, NotificationType.PAYMENT_SUCCESS, "결제 성공 알림 전송 완료");
    }

    // 2. 예약 생성 알림
    @PostMapping("/reservation-created")
    public ResponseDto<NotificationResponseDto> sendReservationCreated(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return sendNotification(requestDto, NotificationType.RESERVATION_CREATED, "예약 생성 알림 전송 완료");
    }

    // 3. 정산 완료 알림
    @PostMapping("/settlement-completed")
    public ResponseDto<NotificationResponseDto> sendSettlementCompleted(
            @RequestBody NotificationCreateRequestDto requestDto) {
        return sendNotification(requestDto, NotificationType.SETTLEMENT_COMPLETE, "정산 완료 알림 전송 완료");
    }

    // 공통 메서드
    private ResponseDto<NotificationResponseDto> sendNotification(
            NotificationCreateRequestDto requestDto, NotificationType type, String successMessage
    ) {
        log.info("[알림 전송 요청] type={}, receiverId={}", type, requestDto.getReceiverId());
        NotificationResponseDto responseDto = notificationService.createNotificationWithType(requestDto, type);
        return ResponseDto.success(successMessage, responseDto);
    }

    // 4. 알림 목록 조회, 읽음/안읽음 필터
    @GetMapping("/{receiverId}")
    public ResponseDto<List<NotificationResponseDto>> getNotifications(
            @PathVariable("receiverId") UUID receiverId,
            @RequestParam(value = "isRead", required = false) Boolean isRead,
            @AuthUser AuthUserInfo authUserInfo
    ) {
        log.info("[알림 목록 조회 요청] receiverId={}, isRead={}, 요청자={}",
                receiverId, isRead, authUserInfo.getUserId());
        // 요청한 알림의 receiverId가 현재 인증된 사용자와 일치하는지 확인
        AuthValidationUtil.validateUserAccess(receiverId, authUserInfo.getUserId());

        List<NotificationResponseDto> notifications =
                notificationService.getNotificationsByReceiverId(receiverId, isRead);
        return ResponseDto.success("알림 목록 조회 성공", notifications);
    }

    // 5. 알림 읽음 처리 기능
    @PatchMapping("/{notificationId}/read")
    public ResponseDto<Void> markAsRead(@PathVariable("notificationId") UUID notificationId) {
        log.info("[알림 읽음 처리 요청] notificationId={}", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseDto.success("알림 읽음 처리 완료");
    }

}
