package com.oman.domain.statistic.entity;

import com.oman.domain.culinary.entity.Culinary;
import com.oman.domain.culinary.entity.Ingredient;
import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CulinaryIngredientStatistic extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culinary_id", nullable = false)
    private Culinary culinary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private int totalVideoCount;

    @Column(nullable = false)
    private int ingredientIncludedVideoCount;

    @Column(nullable = false)
    private double percentage;

}