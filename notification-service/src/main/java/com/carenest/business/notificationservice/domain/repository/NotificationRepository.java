package com.carenest.business.notificationservice.domain.repository;

import com.carenest.business.notificationservice.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query("SELECT n FROM Notification n WHERE n.receiverId = :receiverId ORDER BY n.sentAt DESC")
    List<Notification> findNotificationsByReceiverId(@Param("receiverId") UUID receiverId);
    List<Notification> findNotificationsByReceiverIdAndIsRead(UUID receiverId, Boolean isRead);
}
