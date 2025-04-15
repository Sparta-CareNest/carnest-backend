package com.carenest.business.caregiverservice.infrastructure.repository.querydsl;

import static com.carenest.business.caregiverservice.domain.model.QCaregiver.*;
import static com.carenest.business.caregiverservice.domain.model.category.QCaregiverCategoryLocation.*;
import static com.carenest.business.caregiverservice.domain.model.category.QCaregiverCategoryService.*;
import static com.carenest.business.caregiverservice.domain.model.category.QCategoryLocation.*;
import static com.carenest.business.caregiverservice.domain.model.category.QCategoryService.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CaregiverRepositoryImpl implements CaregiverCustomRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Caregiver> searchByConditions(String location, String service, Pageable pageable) {

		JPAQuery<Caregiver> careQuery = jpaQueryFactory
			.selectFrom(caregiver)
			.leftJoin(caregiver.caregiverCategoryLocations, caregiverCategoryLocation).fetchJoin()
			.leftJoin(caregiver.caregiverCategoryServices, caregiverCategoryService).fetchJoin()
			.where(
				location != null ? caregiverCategoryLocation.categoryLocation.name.eq(location) : null,
				service != null ? caregiverCategoryService.categoryService.name.eq(service) : null,
				caregiver.approvalStatus.eq(true)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		// 정렬
		for(Sort.Order o : pageable.getSort()){
			PathBuilder pathBuilder = new PathBuilder(caregiver.getType(), caregiver.getMetadata());
			careQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
		}

		List<Caregiver> caregivers = careQuery.fetch();
		JPAQuery<Long> caregiverCountQuery = jpaQueryFactory
			.select(caregiver.count())
			.from(caregiver)
			.leftJoin(caregiver.caregiverCategoryServices, caregiverCategoryService)
			.leftJoin(caregiver.caregiverCategoryLocations, caregiverCategoryLocation)
			.where(
				location != null ? caregiverCategoryLocation.categoryLocation.name.eq(location) : null,
				service != null ? caregiverCategoryService.categoryService.name.eq(service) : null,
				caregiver.approvalStatus.eq(true)
			);

		return new PageImpl<>(caregivers,pageable,caregiverCountQuery.fetchOne());
	}

	@Override
	public Optional<Caregiver> findCaregiverWithCategories(UUID userId) {
		Caregiver result = jpaQueryFactory
			.selectFrom(caregiver)
			.leftJoin(caregiver.caregiverCategoryLocations, caregiverCategoryLocation).fetchJoin()
			.leftJoin(caregiver.caregiverCategoryServices, caregiverCategoryService).fetchJoin()
			.where(caregiver.userId.eq(userId))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public Page<Caregiver> findAllCaregivers(Pageable pageable) {
		JPAQuery careQuery = jpaQueryFactory
			.selectFrom(caregiver).distinct()
			.leftJoin(caregiver.caregiverCategoryServices, caregiverCategoryService).fetchJoin()
			.leftJoin(caregiverCategoryService.categoryService, categoryService).fetchJoin()
			.leftJoin(caregiver.caregiverCategoryLocations, caregiverCategoryLocation).fetchJoin()
			.leftJoin(caregiverCategoryLocation.categoryLocation, categoryLocation).fetchJoin()
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		for(Sort.Order o : pageable.getSort()){
			PathBuilder pathBuilder = new PathBuilder(caregiver.getType(), caregiver.getMetadata());
			careQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
		}

		List<Caregiver> caregivers = careQuery.fetch();
		JPAQuery<Long> caregiverCountQuery = jpaQueryFactory
			.select(caregiver.id.countDistinct())
			.from(caregiver);

		return new PageImpl<>(caregivers,pageable,caregiverCountQuery.fetchOne());
	}
}
