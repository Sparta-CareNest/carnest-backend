package com.carenest.business.notificationservice.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                UUID userId = UUID.fromString(query.split("=")[1]);
                sessions.put(userId, session);
                log.info("사용자 WebSocket 연결됨: userId={}, sessionId={}", userId, session.getId());
            } else {
                log.warn("잘못된 WebSocket 연결 요청 (userId 없음): uri={}", session.getUri());
            }
        } catch (Exception e) {
            log.error("WebSocket 연결 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 필요하면 메시지 처리 (우린 수신 전용)
        log.debug("WebSocket 메시지 수신: sessionId={}, payload={}", session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            for (Map.Entry<UUID, WebSocketSession> entry : sessions.entrySet()) {
                if (entry.getValue().getId().equals(session.getId())) {
                    sessions.remove(entry.getKey());
                    log.info("사용자 WebSocket 연결 종료됨: userId={}, sessionId={}, status={}",
                            entry.getKey(), session.getId(), status);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("WebSocket 연결 종료 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 전송 오류: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
        super.handleTransportError(session, exception);
    }

    // 알림 전송 메서드
    public void sendNotification(UUID receiverId, String message) throws IOException {
        WebSocketSession session = sessions.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("WebSocket 알림 전송 성공: userId={}", receiverId);
            } catch (IOException e) {
                log.error("WebSocket 알림 전송 실패: userId={}, error={}", receiverId, e.getMessage());
                throw e;
            }
        } else {
            log.warn("사용자 WebSocket 연결 없음 (알림 전송 실패): userId={}", receiverId);
        }
    }
}
