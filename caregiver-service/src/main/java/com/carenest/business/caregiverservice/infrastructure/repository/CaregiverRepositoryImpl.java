package com.carenest.business.caregiverservice.infrastructure.repository;

import com.carenest.business.caregiverservice.domain.repository.CaregiverCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CaregiverRepositoryImpl implements CaregiverCustomRepository {
	private final JPAQueryFactory jpaQueryFactory;
}
