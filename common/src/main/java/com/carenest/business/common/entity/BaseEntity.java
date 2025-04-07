package com.carenest.business.common.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime deletedAt;

	@CreatedDate
	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedAt;


	@Column(nullable = false)
	@Builder.Default
	private Boolean isDeleted = false;

	/**
	 * 엔티티 soft delete
	 * @param deleteUserId 삭제자 id
	 */
	public void softDelete(Long deleteUserId) {
		this.deletedAt = LocalDateTime.now();
		this.isDeleted = true;
	}

	/**
	 * 엔티티 soft delete 취소
	 */
	public void restore() {
		this.deletedAt = null;
		this.isDeleted = false;
	}
}
