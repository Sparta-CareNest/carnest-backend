package com.carenest.business.chatservice.domain.model;

import com.carenest.business.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "p_chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "guardian_id")
    private UUID guardianId;

    @Column(name = "caregiver_id")
    private UUID caregiverId;

}
