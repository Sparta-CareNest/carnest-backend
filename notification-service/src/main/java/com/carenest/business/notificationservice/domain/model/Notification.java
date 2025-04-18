package com.carenest.business.notificationservice.domain.model;

import com.carenest.business.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public static Notification create(UUID receiverId, NotificationType type, String content) {
        return Notification.builder()
                .receiverId(receiverId)
                .type(type)
                .content(content)
                .isRead(false) // 항상 false로 시작
                .sentAt(LocalDateTime.now()) // 생성 시점 시간
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
