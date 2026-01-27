package com.oman.domain.recipe.dto.response;

import java.util.List;

public record CulinaryRecommendationDto(
    String culinaryName,
    List<RecipeDetail> exactRecipes,    // 정확히 일치하는 영상들
    List<RecipeDetail> surplusRecipes,  // 재료가 남는 영상들
    List<RecipeDetail> missingRecipes   // 재료가 더 필요한 영상들
) {}