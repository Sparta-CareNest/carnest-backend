package com.carenest.business.reviewservice.domain.model;


import com.carenest.business.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_review")
@SuperBuilder
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID reviewId;

    @Column(nullable = false)
    private UUID reservationId;

    @Column(nullable = false)
    private UUID userId; // 리뷰 작성자

    @Column(nullable = false)
    private UUID caregiverId; // 리뷰 대상

    private double rating;

    private String content;

}


