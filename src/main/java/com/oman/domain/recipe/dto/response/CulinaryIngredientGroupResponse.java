package com.oman.domain.recipe.dto.response;

import com.oman.domain.statistic.dto.CulinaryIngredientResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CulinaryIngredientGroupResponse {
    private List<CulinaryIngredientResponse> mainIngredients;
    private List<CulinaryIngredientResponse> subIngredients;
    private List<CulinaryIngredientResponse> otherIngredients;
}