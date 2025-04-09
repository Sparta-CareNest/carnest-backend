package com.carenest.business.reservationservice.domain.repository;

import com.carenest.business.reservationservice.domain.model.QReservation;
import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        // 삭제된 예약 제외
        JPAQuery<Reservation> query = queryFactory
                .selectFrom(reservation)
                .where(builder);

        // 총 개수 조회
        long total = query.fetchCount();

        // 페이징 및 정렬 적용
        List<Reservation> results = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(results, pageable, total);
    }
}