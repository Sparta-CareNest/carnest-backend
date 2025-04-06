package com.carenest.business.matchingservice.infrastructure.repository;

import com.carenest.business.matchingservice.domain.repository.CaregiverCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CaregiverRepositoryImpl implements CaregiverCustomRepository {
	private final JPAQueryFactory jpaQueryFactory;
}
