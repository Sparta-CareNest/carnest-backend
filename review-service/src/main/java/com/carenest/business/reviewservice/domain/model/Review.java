package com.carenest.business.reviewservice.domain.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_review")
@NoArgsConstructor
public class Review {

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


