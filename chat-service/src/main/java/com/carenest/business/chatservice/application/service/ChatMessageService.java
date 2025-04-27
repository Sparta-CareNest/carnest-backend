package com.carenest.business.chatservice.application.service;

import com.carenest.business.chatservice.application.dto.request.ChatMessageRequestDto;

public interface ChatMessageService {
    void sendMessage(ChatMessageRequestDto requestDto);
}
