package com.carenest.business.notificationservice.infrastructure.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    // 연결된 사용자들 저장
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // URL 파라미터에서 userId 추출 (예: /ws/notifications?userId=abc123)
        String query = session.getUri().getQuery();
        UUID userId = UUID.fromString(query.split("=")[1]);
        sessions.put(userId, session);
        System.out.println("✅ 연결된 사용자: " + userId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 필요하면 메시지 처리 (우린 수신 전용)
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
        System.out.println("❌ 연결 종료됨: " + session.getId());
    }

    // 알림 전송 메서드
    public void sendNotification(UUID receiverId, String message) throws IOException {
        WebSocketSession session = sessions.get(receiverId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        } else {
            System.out.println("⚠️ 사용자 연결 안됨: " + receiverId);
        }
    }
}
