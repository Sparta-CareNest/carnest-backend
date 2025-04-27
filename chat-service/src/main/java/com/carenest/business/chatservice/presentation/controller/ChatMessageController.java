package com.carenest.business.chatservice.presentation.controller;

import com.carenest.business.chatservice.application.dto.request.ChatMessageRequestDto;
import com.carenest.business.chatservice.application.dto.response.ChatMessageResponseDto;
import com.carenest.business.chatservice.application.service.ChatMessageService;
import com.carenest.business.chatservice.application.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/message") // 클라이언트는 /pub/chat/message 로 메시지 보냄
    public void sendMessage(@Payload ChatMessageRequestDto messageDto) {
        chatMessageService.sendMessage(messageDto);
    }
}
