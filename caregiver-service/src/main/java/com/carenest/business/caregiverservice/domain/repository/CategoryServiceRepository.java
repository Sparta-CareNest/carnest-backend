package com.carenest.business.caregiverservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.caregiverservice.domain.model.category.CategoryService;

public interface CategoryServiceRepository extends JpaRepository<CategoryService, Long> {
}
