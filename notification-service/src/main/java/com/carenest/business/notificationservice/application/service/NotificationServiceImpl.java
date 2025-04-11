package com.carenest.business.notificationservice.application.service;

import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.domain.model.Notification;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import com.carenest.business.notificationservice.domain.repository.NotificationRepository;
import com.carenest.business.notificationservice.infrastructure.config.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public NotificationResponseDto createNotificationWithType(
            NotificationCreateRequestDto requestDto,
            NotificationType notificationType
    ) {
        Notification notification = Notification.create(
                requestDto.getReceiverId(),
                notificationType,
                notificationType.getMessage()
        );

        Notification saved = notificationRepository.save(notification);

        try {
            // JSON 문자열로 전송하거나 간단한 텍스트로 전송해도 됨
            String message = "[알림] " + saved.getContent();
            webSocketHandler.sendNotification(saved.getReceiverId(), message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new NotificationResponseDto(
                saved.getId(),
                saved.getType(),
                saved.getContent(),
                saved.getSentAt(),
                saved.isRead()
        );
    }

    @Override
    public List<NotificationResponseDto> getNotificationsByReceiverId(UUID receiverId) {
        List<Notification> notifications = notificationRepository.findNotificationsByReceiverId(receiverId);

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
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다."));
        notification.markAsRead();
        notificationRepository.save(notification);
    }
}
