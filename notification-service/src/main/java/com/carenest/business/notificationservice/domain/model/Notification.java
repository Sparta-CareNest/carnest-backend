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

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public static Notification create(UUID receiverId, NotificationType type, String content) {
        return Notification.builder()
                .receiverId(receiverId)
                .type(type)
                .content(content)
                .isRead(false) // 항상 false로 시작
                .build();
    }

    // 읽음 처리
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
        }
    }

    // 자동 시간 설정
    @PrePersist
    protected void onPrePersist() {
        this.sentAt = LocalDateTime.now();
    }
}
