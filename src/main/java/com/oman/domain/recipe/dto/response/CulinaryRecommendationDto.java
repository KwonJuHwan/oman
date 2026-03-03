package com.oman.domain.recipe.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CulinaryRecommendationDto {
    private String name;
    private String status;
    private List<IngredientSimpleDto> ingredients;
}