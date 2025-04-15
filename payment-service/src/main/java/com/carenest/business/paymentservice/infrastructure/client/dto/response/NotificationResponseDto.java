package com.carenest.business.paymentservice.infrastructure.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private UUID notificationId;
    private String type;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
}