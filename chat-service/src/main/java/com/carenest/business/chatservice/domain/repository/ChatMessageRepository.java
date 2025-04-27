package com.carenest.business.chatservice.domain.repository;

import com.carenest.business.chatservice.domain.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
}
