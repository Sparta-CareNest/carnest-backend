package com.carenest.business.caregiverservice.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;

public interface CategoryLocationRepository extends JpaRepository<CategoryLocation, Long> {
	Long id(Long id);
}
