package com.oman.domain.recipe.controller;

import com.oman.domain.recipe.dto.request.IngredientRequest;
import com.oman.domain.recipe.dto.response.CulinaryRecommendationDto;
import com.oman.domain.recipe.service.RecipeIntegrationService;
import com.oman.domain.statistic.dto.CulinaryIngredientResponse;
import com.oman.domain.youtube.repository.YoutubeVideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeIntegrationController {

    private final RecipeIntegrationService recipeIntegrationService;

    /**
     * 요리별 재료 통계 정보 조회
     */
    @GetMapping("/culinary")
    public ResponseEntity<List<CulinaryIngredientResponse>> getStatistics(
        @RequestParam("name") String culinaryName) {

        List<CulinaryIngredientResponse> response =
            recipeIntegrationService.getIngredientStatistics(culinaryName);

        return ResponseEntity.ok(response);
    }

    /**
     * 재료 리스트 기반 다중 요리 추천
     */
    @PostMapping("/recommendations")
    public ResponseEntity<List<CulinaryRecommendationDto>> getCulinaryRecommendations(
        @RequestBody IngredientRequest request) {

        List<CulinaryRecommendationDto> response =
            recipeIntegrationService.getCulinaryRecommendations(request.ingredientIds());

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 요리에 대한 상세 영상 추천
     */
    @PostMapping("/recommendations/{culinaryName}")
    public ResponseEntity<CulinaryRecommendationDto> getRecommendedVideos(
        @PathVariable String culinaryName,
        @RequestBody IngredientRequest request) {

        CulinaryRecommendationDto response =
            recipeIntegrationService.getRecommendedVideos(culinaryName, request.ingredientIds());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/ingredients/{culinaryName}")
    public ResponseEntity<List<Long>> getRandomIngredientIds(@PathVariable String culinaryName) {
        List<Long> ingredientIds = recipeIntegrationService.getRandomVideoIngredientIds(culinaryName);
        return ResponseEntity.ok(ingredientIds);
    }
}
