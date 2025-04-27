package com.carenest.business.notificationservice.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
