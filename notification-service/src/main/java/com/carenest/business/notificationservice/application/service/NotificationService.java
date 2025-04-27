package com.carenest.business.notificationservice.application.service;

import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.domain.model.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationResponseDto createNotificationWithType(NotificationCreateRequestDto requestDto, NotificationType notificationType);
    List<NotificationResponseDto> getNotificationsByReceiverId(UUID receiverId, Boolean isRead);
    void markAsRead(UUID notificationId);

}
