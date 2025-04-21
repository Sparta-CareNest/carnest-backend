package com.carenest.business.caregiverservice.domain.model;

import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.carenest.business.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CaregiverApproval extends BaseEntity {

	@Id
	@Column(name = "reservation_id")
	private UUID reservationId;

	@Column(name = "caregiver_id", nullable = false)
	private UUID caregiverId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private Status status = Status.PENDING;

	@Column(length = 255)
	private String message;


	public enum Status {
		PENDING,
		APPROVED,
		REJECTED
	}
}

