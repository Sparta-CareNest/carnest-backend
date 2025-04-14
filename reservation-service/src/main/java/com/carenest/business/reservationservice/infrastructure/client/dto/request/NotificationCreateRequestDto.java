package com.carenest.business.reservationservice.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequestDto {
    private UUID receiverId;
    private String content;
}