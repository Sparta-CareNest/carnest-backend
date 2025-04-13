package com.carenest.business.notificationservice.application.service;

import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.application.dto.response.UserInfoResponseDto;
import com.carenest.business.notificationservice.domain.model.Notification;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import com.carenest.business.notificationservice.domain.repository.NotificationRepository;
import com.carenest.business.notificationservice.infrastructure.client.UserClient;
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
    private final UserClient userClient;

    @Override
    public NotificationResponseDto createNotificationWithType(
            NotificationCreateRequestDto requestDto,
            NotificationType notificationType
    ) {
        // 유저 존재 여부 확인
        boolean isValidUser = userClient.validateUser(requestDto.getReceiverId());
        if (!isValidUser) {
            throw new IllegalArgumentException("존재하지 않는 유저에게 알림을 보낼 수 없습니다.");
        }

        // 유저 닉네임 가져오기
        UserInfoResponseDto userInfo = userClient.getUserInfo(requestDto.getReceiverId());
        String nickname = userInfo.getNickname();

        // 알림 내용에 닉네임 포하
        String content = "[" + nickname + "] " + notificationType.getMessage();

        Notification notification = Notification.create(
                requestDto.getReceiverId(),
                notificationType,
                content
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
