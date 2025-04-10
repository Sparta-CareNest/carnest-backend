package com.carenest.business.chatservice.application.service;

import com.carenest.business.chatservice.application.dto.request.ChatRoomCreateRequestDto;
import com.carenest.business.chatservice.application.dto.response.ChatRoomResponseDto;
import com.carenest.business.chatservice.domain.model.ChatRoom;
import com.carenest.business.chatservice.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoomResponseDto createChatRoom(ChatRoomCreateRequestDto requestDto) {
        ChatRoom chatRoom = ChatRoom.builder()
                .guardianId(requestDto.getGuardianId())
                .caregiverId(requestDto.getCaregiverId())
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);
        return ChatRoomResponseDto.from(saved);
    }

    @Override
    public List<ChatRoomResponseDto> getChatRoomsByUserId(UUID userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserId(userId);
        return chatRooms.stream()
                .map(ChatRoomResponseDto::from)
                .toList();
    }
}
