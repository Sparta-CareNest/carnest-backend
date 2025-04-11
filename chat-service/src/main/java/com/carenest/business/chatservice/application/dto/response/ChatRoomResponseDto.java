package com.carenest.business.chatservice.application.dto.response;

import com.carenest.business.chatservice.domain.model.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {
    private UUID id;
    private UUID caregiverId;
    private UUID guardianId;

    public static ChatRoomResponseDto from(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
                .id(chatRoom.getId())
                .guardianId(chatRoom.getGuardianId())
                .caregiverId(chatRoom.getCaregiverId())
                .build();
    }
}
