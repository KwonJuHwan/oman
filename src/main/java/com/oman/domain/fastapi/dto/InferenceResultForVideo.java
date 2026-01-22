package com.oman.domain.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class InferenceResultForVideo {
    @JsonProperty("extracted_ingredients")
    private List<ExtractedIngredient> extractedIngredients;
}
