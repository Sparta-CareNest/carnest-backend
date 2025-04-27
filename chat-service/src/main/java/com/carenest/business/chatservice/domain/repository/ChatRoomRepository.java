package com.carenest.business.chatservice.domain.repository;

import com.carenest.business.chatservice.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    @Query("SELECT c FROM ChatRoom c WHERE c.guardianId = :userId OR c.caregiverId = :userId")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") UUID userId);
}
