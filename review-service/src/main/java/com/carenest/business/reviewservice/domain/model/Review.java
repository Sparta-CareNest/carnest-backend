package com.carenest.business.reviewservice.domain.model;


import com.carenest.business.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    public void update(@Min(1) @Max(5) double rating, @NotBlank String content) {
        this.rating = rating;
        this.content = content;
    }
}


