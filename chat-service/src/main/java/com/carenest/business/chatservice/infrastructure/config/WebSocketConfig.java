package com.carenest.business.chatservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 메시지 핸들링 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // WebSocket 연결을 위한 엔드포인트를 등록하는 메서드
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // 클라이언트가 연결할 WebSocket 엔드포인트 (ex. ws://localhost:8080/ws/chat)
                .setAllowedOriginPatterns("*") // 모든 도메인에서의 연결 허용 (CORS 허용)
                .withSockJS(); // SockJS fallback 옵션 (브라우저 지원 안 될 경우 대비)
    }

    // 메시지 브로커 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // 구독 경로 prefix. ex) /sub/chat/rooms/{roomId}
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메시지를 보낼 때 사용할 prefix. ex) /pub/chat/message
    }
}
