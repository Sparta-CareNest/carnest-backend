package com.carenest.business.common.event.notification;

import com.carenest.business.common.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class NotificationEvent extends BaseEvent {
    private UUID receiverId;
    private String notificationType;
    private String content;

    @Builder
    public NotificationEvent(UUID receiverId, String notificationType, String content) {
        super("NOTIFICATION");
        this.receiverId = receiverId;
        this.notificationType = notificationType;
        this.content = content;
    }
}