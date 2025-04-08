package com.carenest.business.caregiverservice.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryService;
import com.carenest.business.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_caregiver")
@EntityListeners(AuditingEntityListener.class)
public class Caregiver extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// 간병인 생성할때 매핑
	@Column(name = "user_id")
	private UUID userId;

	@OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CaregiverCategoryLocation> caregiverCategoryLocations = new ArrayList<>();

	@OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CaregiverCategoryService> caregiverCategoryServices = new ArrayList<>();

	@Column(length = 1000)
	private String description;
	@Column
	private Double rating;
	@Column
	private Integer experienceYears;
	@Column
	private Integer pricePerHour;
	@Column
	private Integer pricePerDay;
	@Column
	@Builder.Default
	private Boolean approvalStatus = false;
	@Column
	private String submittedDocuments;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GenderType gender;

	public void clearCategoryServices() {
		this.caregiverCategoryServices.clear();
	}

	public void clearCategoryLocation() {
		this.caregiverCategoryLocations.clear();
	}
}
