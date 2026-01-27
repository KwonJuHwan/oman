package com.oman.domain.recipe.dto.request;

import java.util.List;


public record IngredientRequest(
    List<Long> ingredientIds
) {

}
