package com.carenest.business.chatservice.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequestDto {
    private UUID guardianId;
    private UUID caregiverId;
}
