package com.carenest.business.caregiverservice.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carenest.business.caregiverservice.infrastructure.s3.Uuid;

public interface UuidRepository extends JpaRepository<Uuid, Long> {
}
