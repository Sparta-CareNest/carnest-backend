package com.carenest.business.notificationservice.application.service;

import com.carenest.business.common.exception.BaseException;
import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.domain.model.Notification;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import com.carenest.business.notificationservice.domain.repository.NotificationRepository;
import com.carenest.business.notificationservice.exception.NotificationErrorCode;
import com.carenest.business.notificationservice.infrastructure.client.UserClient;
import com.carenest.business.notificationservice.infrastructure.config.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final UserClient userClient;

    @Override
    public NotificationResponseDto createNotificationWithType(
            NotificationCreateRequestDto requestDto,
            NotificationType notificationType
    ) {
        // 사용자 존재 여부 확인
        Boolean isValidUser = userClient.validateUser(requestDto.getReceiverId());
        if (!isValidUser) {
            throw new BaseException(NotificationErrorCode.USER_NOT_FOUND);
        }

        String content = "[" + requestDto.getReceiverId() + "] " + notificationType.getMessage();

        // 알림 객체 생성
        Notification notification = Notification.create(
                requestDto.getReceiverId(),
                notificationType,
                content
        );

        // 알림 저장
        Notification saved = notificationRepository.save(notification);

        try {
            // WebSocket 알림 전송
            String message = "[알림] " + saved.getContent();
            webSocketHandler.sendNotification(saved.getReceiverId(), message);
        } catch (IOException e) {
            log.error("WebSocket 알림 전송 실패: {}", e.getMessage());
        }

        // 알림 응답 반환
        return new NotificationResponseDto(
                saved.getId(),
                saved.getType(),
                saved.getContent(),
                saved.getSentAt(),
                saved.isRead()
        );
    }

    @Override
    public List<NotificationResponseDto> getNotificationsByReceiverId(UUID receiverId, Boolean isRead) {
        List<Notification> notifications;

        if (isRead == null) {
            notifications = notificationRepository.findNotificationsByReceiverId(receiverId);
        } else {
            notifications = notificationRepository.findNotificationsByReceiverIdAndIsRead(receiverId, isRead);
        }

        return notifications.stream()
                .map(n -> new NotificationResponseDto(
                        n.getId(),
                        n.getType(),
                        n.getContent(),
                        n.getSentAt(),
                        n.isRead()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();
        notificationRepository.save(notification);
    }
}
