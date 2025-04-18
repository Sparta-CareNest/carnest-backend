package com.carenest.business.notificationservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.exception.BaseException;
import com.carenest.business.common.exception.CommonErrorCode;
import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.application.service.NotificationService;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
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
        return ResponseDto.success("결제 성공 알림 전송 완료", responseDto);
    }

    // 2. 예약 생성 알림
    @PostMapping("/reservation-created")
    public ResponseDto<NotificationResponseDto> sendReservationCreated(
            @RequestBody NotificationCreateRequestDto requestDto) {
        NotificationResponseDto responseDto = notificationService.createNotificationWithType(
                requestDto, NotificationType.RESERVATION_CREATED);
        return ResponseDto.success("예약 생성 알림 전송 완료", responseDto);
    }

    // 3. 정산 완료 알림
    @PostMapping("/settlement-completed")
    public ResponseDto<NotificationResponseDto> sendSettlementCompleted(
            @RequestBody NotificationCreateRequestDto requestDto) {
        NotificationResponseDto responseDto = notificationService.createNotificationWithType(
                requestDto, NotificationType.SETTLEMENT_COMPLETE);
        return ResponseDto.success("정산 완료 알림 전송 완료", responseDto);
    }

    // 4. 알림 목록 조회
    @GetMapping("/{receiverId}")
    public ResponseDto<List<NotificationResponseDto>> getNotifications(
            @PathVariable("receiverId") UUID receiverId,
            @AuthUser AuthUserInfo authUserInfo
    ) {
        // 요청한 알림의 receiverId가 현재 인증된 사용자와 일치하는지 확인
        if (!receiverId.equals(authUserInfo.getUserId())) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        List<NotificationResponseDto> notifications = notificationService.getNotificationsByReceiverId(receiverId);
        return ResponseDto.success("알림 목록 조회 성공", notifications);
    }

    // 5. 알림 읽음 처리 기능
    @PatchMapping("/{notificationId}/read")
    public ResponseDto<Void> markAsRead(@PathVariable("notificationId") UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseDto.success("알림 읽음 처리 완료", null);
    }

}
