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
public class IngredientStatistic extends BaseTimeEntity {
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

    public void updateStatistics(int totalVideoCount,int ingredientIncludedVideoCount, double percentage){
        this.totalVideoCount = totalVideoCount;
        this.ingredientIncludedVideoCount = ingredientIncludedVideoCount;
        this.percentage = percentage;
    }

}