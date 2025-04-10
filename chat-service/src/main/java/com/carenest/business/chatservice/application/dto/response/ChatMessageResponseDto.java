package com.carenest.business.chatservice.application.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto {
    private UUID id;
    private UUID chatRoomId;
    private UUID senderId;
    private String message;
    private LocalDateTime sentAt;
}
