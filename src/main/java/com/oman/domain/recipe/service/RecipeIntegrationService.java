package com.oman.domain.recipe.service;

import com.oman.domain.culinary.service.IngredientQueryService;
import com.oman.domain.recipe.dto.response.CulinaryRecommendationDto;
import com.oman.domain.recipe.dto.response.RecipeDetail;
import com.oman.domain.statistic.dto.CulinaryIngredientResponse;
import com.oman.domain.statistic.service.IngredientStatisticQueryService;
import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import com.oman.domain.youtube.service.YoutubeCommandService;
import com.oman.domain.youtube.service.YoutubeQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeIntegrationService {
    private final IngredientStatisticQueryService statisticsQueryService;
    private final IngredientQueryService ingredientQueryService;
    private final YoutubeQueryService youtubeQueryService;
    private final YoutubeCommandService youtubeCommandService;

    public List<CulinaryIngredientResponse> getIngredientStatistics(String culinaryName){
        return statisticsQueryService.getIngredientStatistics(culinaryName);
    }

    public List<Long> getRandomVideoIngredientIds(String culinaryName) {
        return youtubeCommandService.getRandomVideoIngredientIds(culinaryName);
    }


    /**
     * 재료 리스트 기반 다중 요리 추천
     */
    public List<CulinaryRecommendationDto> getCulinaryRecommendations(List<Long> userIngredientIds) {
        Set<Long> userSet = new HashSet<>(userIngredientIds);
        List<YoutubeVideoMeta> allMetas = youtubeQueryService.findAllWithIngredients(userIngredientIds);
        Map<Long, String> ingredientNameMap = ingredientQueryService.findAll();

        Map<Long, CulinaryGroup> groupByCulinary = new HashMap<>();

        for (YoutubeVideoMeta meta : allMetas) {
            Long culinaryId = meta.getCulinary().getId();
            CulinaryGroup group = groupByCulinary.computeIfAbsent(culinaryId,
                id -> new CulinaryGroup(meta.getCulinary().getName()));

            categorizeVideo(group, meta, userSet, ingredientNameMap);
        }

        return groupByCulinary.values().stream()
            .filter(CulinaryGroup::hasAnyRecipe)
            .map(CulinaryGroup::toDto)
            .toList();
    }

    /**
     * 특정 요리에 대한 상세 영상 추천
     */
    public CulinaryRecommendationDto getRecommendedVideos(String culinaryName, List<Long> userIngredientIds) {
        Set<Long> userSet = new HashSet<>(userIngredientIds);
        List<YoutubeVideoMeta> metas = youtubeQueryService.findAllByCulinaryNameWithIngredients(culinaryName);

        if (metas.isEmpty()) {
            return new CulinaryRecommendationDto(culinaryName, List.of(), List.of(), List.of());
        }

        Map<Long, String> ingredientNameMap = prepareIngredientNameMap(metas);
        CulinaryGroup group = new CulinaryGroup(culinaryName);

        for (YoutubeVideoMeta meta : metas) {
            categorizeVideo(group, meta, userSet, ingredientNameMap);
        }

        return group.toDto();
    }

    /**
     * 비디오를 조건에 따라 분류하고 그룹에 추가
     */
    private void categorizeVideo(CulinaryGroup group, YoutubeVideoMeta meta,
        Set<Long> userSet, Map<Long, String> nameMap) {
        Set<Long> videoSet = meta.getIngredientIds();

        // 1. 정확히 일치
        if (userSet.equals(videoSet)) {
            group.addExact(RecipeDetail.of(meta, Collections.emptyList(), 0));
        }
        // 2. 재료가 남음 (Video ⊂ User)
        else if (userSet.containsAll(videoSet)) {
            Set<Long> surplusIds = new HashSet<>(userSet);
            surplusIds.removeAll(videoSet);
            group.addSurplus(RecipeDetail.of(meta, mapNames(surplusIds, nameMap), surplusIds.size()));
        }
        // 3. 재료가 부족함 (상한선 3개)
        else {
            Set<Long> missingIds = new HashSet<>(videoSet);
            missingIds.removeAll(userSet);
            if (missingIds.size() <= 3) {
                group.addMissing(RecipeDetail.of(meta, mapNames(missingIds, nameMap), missingIds.size()));
            }
        }
    }

    private Map<Long, String> prepareIngredientNameMap(List<YoutubeVideoMeta> metas) {
        Set<Long> allIngredientIds = metas.stream()
            .flatMap(m -> m.getIngredientIds().stream())
            .collect(Collectors.toSet());

        return ingredientQueryService.findAllById(allIngredientIds).stream()
            .collect(Collectors.toMap(Ingredient::getId, Ingredient::getName));
    }

    private List<String> mapNames(Set<Long> ids, Map<Long, String> nameMap) {
        return ids.stream().map(nameMap::get).toList();
    }

    @Getter
    static class CulinaryGroup {
        private final String culinaryName;
        private final List<RecipeDetail> exact = new ArrayList<>();
        private final List<RecipeDetail> surplus = new ArrayList<>();
        private final List<RecipeDetail> missing = new ArrayList<>();

        public CulinaryGroup(String culinaryName) { this.culinaryName = culinaryName; }

        public void addExact(RecipeDetail detail) { exact.add(detail); }
        public void addSurplus(RecipeDetail detail) { surplus.add(detail); }
        public void addMissing(RecipeDetail detail) { missing.add(detail); }

        public boolean hasAnyRecipe() {
            return !exact.isEmpty() || !surplus.isEmpty() || !missing.isEmpty();
        }

        public CulinaryRecommendationDto toDto() {
            surplus.sort(Comparator.comparingInt(RecipeDetail::diffCount));
            missing.sort(Comparator.comparingInt(RecipeDetail::diffCount));
            return new CulinaryRecommendationDto(culinaryName, exact, surplus, missing);
        }

    }
}
