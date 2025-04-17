package com.carenest.business.reservationservice.domain.repository;

import com.carenest.business.reservationservice.domain.model.QReservation;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ReservationRepositoryCustomImpl extends QuerydslRepositorySupport implements ReservationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public ReservationRepositoryCustomImpl() {
        super(Reservation.class);
    }

    @Override
    public Page<Reservation> findBySearchCriteria(
            UUID guardianId,
            UUID caregiverId,
            String patientName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ReservationStatus status,
            Pageable pageable
    ) {
        QReservation reservation = QReservation.reservation;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        BooleanBuilder builder = new BooleanBuilder();

        // 검색 조건 적용
        if (guardianId != null) {
            builder.and(reservation.guardianId.eq(guardianId));
        }

        if (caregiverId != null) {
            builder.and(reservation.caregiverId.eq(caregiverId));
        }

        if (StringUtils.hasText(patientName)) {
            builder.and(reservation.patientName.containsIgnoreCase(patientName));
        }

        if (startDate != null) {
            builder.and(reservation.startedAt.goe(startDate));
        }

        if (endDate != null) {
            builder.and(reservation.endedAt.loe(endDate));
        }

        if (status != null) {
            builder.and(reservation.status.eq(status));
        }

        // 쿼리 생성
        JPAQuery<Reservation> query = queryFactory
                .selectFrom(reservation)
                .where(builder);

        // 총 개수 조회
        long total = query.fetch().size();

        // 정렬 적용
        query.orderBy(getOrderSpecifier(pageable.getSort()));

        // 페이징 적용
        List<Reservation> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        // 기본값으로 생성일시 내림차순
        if (sort.isUnsorted()) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, QReservation.reservation.createdAt)
            };
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        PathBuilder<Reservation> entityPath = new PathBuilder<>(Reservation.class, "reservation");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            // PathBuilder를 사용하여 필드 경로 생성
            orders.add(new OrderSpecifier(direction, entityPath.get(order.getProperty())));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}