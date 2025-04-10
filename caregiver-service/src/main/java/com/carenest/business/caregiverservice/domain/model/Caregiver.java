package com.carenest.business.caregiverservice.domain.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CaregiverCategoryService;
import com.carenest.business.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@SQLRestriction("is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_caregiver")
@EntityListeners(AuditingEntityListener.class)
public class Caregiver extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// 간병인 생성할때 매핑
	@Column(name = "user_id",unique = true)
	private UUID userId;

	@OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<CaregiverCategoryLocation> caregiverCategoryLocations = new HashSet<>();

	@OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<CaregiverCategoryService> caregiverCategoryServices = new HashSet<>();

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

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "p_caregiver_document_urls", joinColumns = @JoinColumn(name = "caregiver_id"))
	@Column(name = "document_url")
	@Builder.Default
	private List<String> submittedDocuments = new ArrayList<>();

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GenderType gender;

	public void clearCategoryServices() {
		this.caregiverCategoryServices.clear();
	}

	public void clearCategoryLocation() {
		this.caregiverCategoryLocations.clear();
	}

	public void updateApprovalStatus(boolean check) {
		this.approvalStatus = check;
	}
}
