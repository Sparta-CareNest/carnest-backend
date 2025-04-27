package com.carenest.business.notificationservice.application.dto.response;

import com.carenest.business.notificationservice.domain.model.Notification;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private UUID notificationId;
    private NotificationType type;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .sentAt(notification.getSentAt())
                .isRead(notification.isRead())
                .build();
    }
}
