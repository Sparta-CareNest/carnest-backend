package com.carenest.business.common.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public abstract class BaseEvent {
    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;

    protected BaseEvent(String eventType) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
}