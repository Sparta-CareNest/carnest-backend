package com.carenest.business.chatservice.application.service;

import com.carenest.business.chatservice.application.dto.request.ChatRoomCreateRequestDto;
import com.carenest.business.chatservice.application.dto.response.ChatRoomResponseDto;

import java.util.List;
import java.util.UUID;

public interface ChatRoomService {
    ChatRoomResponseDto createChatRoom(ChatRoomCreateRequestDto requestDto);
    List<ChatRoomResponseDto> getChatRoomsByUserId(UUID userId);
}
