package com.carenest.business.notificationservice.application.dto.request;

import com.carenest.business.notificationservice.domain.model.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequestDto {
    private UUID receiverId;
    private String content;
}
