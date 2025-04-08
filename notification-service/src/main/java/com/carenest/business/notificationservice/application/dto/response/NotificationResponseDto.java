package com.carenest.business.notificationservice.application.dto.response;

import com.carenest.business.notificationservice.domain.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
public class NotificationResponseDto {
    private UUID notificationId;
    private NotificationType type;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
}
