package com.carenest.business.notificationservice.application.service;

import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.NotificationResponseDto;
import com.carenest.business.notificationservice.domain.model.NotificationType;

public interface NotificationService {
    NotificationResponseDto createNotificationWithType(NotificationCreateRequestDto requestDto, NotificationType notificationType);
}
