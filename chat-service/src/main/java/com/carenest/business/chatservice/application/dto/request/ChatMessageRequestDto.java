package com.carenest.business.chatservice.application.dto.request;

import lombok.*;

import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequestDto {
    private UUID chatRoomId;
    private UUID senderId;
    private String message;
}
