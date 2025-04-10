package com.carenest.business.chatservice.presentation.controller;

import com.carenest.business.chatservice.application.dto.request.ChatRoomCreateRequestDto;
import com.carenest.business.chatservice.application.dto.response.ChatRoomResponseDto;
import com.carenest.business.chatservice.application.service.ChatRoomService;
import com.carenest.business.common.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 1. 채팅방 생성
    @PostMapping("/rooms")
    public ResponseDto<ChatRoomResponseDto> createChatRoom(
            @RequestBody ChatRoomCreateRequestDto requestDto
    ){
        ChatRoomResponseDto createdRoom = chatRoomService.createChatRoom(requestDto);
        return ResponseDto.success(createdRoom);
    }

    /*
    2. 채팅방 목록 조회
    사용자가 참여 중인 채팅방 모두 조회(guardian이든 caregiver이든)
     */
    @GetMapping("/rooms")
    public ResponseDto<List<ChatRoomResponseDto>> getChatRoomsByUserId(
            @RequestParam("userId") UUID userId
    ){
        List<ChatRoomResponseDto> chatRooms = chatRoomService.getChatRoomsByUserId(userId);
        return ResponseDto.success(chatRooms);
    }
}
