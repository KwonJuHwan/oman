package com.oman.domain.statistic.dto;

import com.oman.domain.statistic.entity.IngredientStatistic;

public record CulinaryIngredientResponse(
    String ingredientName,
    int totalVideoCount,
    int ingredientIncludedVideoCount,
    double percentage
) {
    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static CulinaryIngredientResponse from(IngredientStatistic statistic) {
        return new CulinaryIngredientResponse(
            statistic.getIngredient().getName(),
            statistic.getTotalVideoCount(),
            statistic.getIngredientIncludedVideoCount(),
            statistic.getPercentage()
        );
    }
}