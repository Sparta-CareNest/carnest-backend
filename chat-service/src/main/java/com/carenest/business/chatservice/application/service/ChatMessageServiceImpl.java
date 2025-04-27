package com.carenest.business.chatservice.application.service;

import com.carenest.business.chatservice.application.dto.request.ChatMessageRequestDto;
import com.carenest.business.chatservice.application.dto.response.ChatMessageResponseDto;
import com.carenest.business.chatservice.domain.model.ChatMessage;
import com.carenest.business.chatservice.domain.model.ChatRoom;
import com.carenest.business.chatservice.domain.repository.ChatMessageRepository;
import com.carenest.business.chatservice.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendMessage(ChatMessageRequestDto requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(requestDto.getSenderId())
                .message(requestDto.getMessage())
                .build();

        chatMessageRepository.save(message);

        ChatMessageResponseDto responseDto = ChatMessageResponseDto.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .build();

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + chatRoom.getId(),
                responseDto
        );
    }
}
