package com.oman.domain.recipe.service;

import com.oman.domain.culinary.service.IngredientQueryService;
import com.oman.domain.recipe.dto.response.CulinaryIngredientGroupResponse;
import com.oman.domain.recipe.dto.response.CulinaryRecommendationDto;
import com.oman.domain.recipe.dto.response.IngredientSimpleDto;
import com.oman.domain.recipe.dto.response.VideoRecommendationResponseDto;
import com.oman.domain.recipe.dto.response.VideoResponseDto;
import com.oman.domain.statistic.dto.CulinaryIngredientResponse;
import com.oman.domain.statistic.service.IngredientStatisticQueryService;
import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import com.oman.domain.youtube.service.YoutubeCommandService;
import com.oman.domain.youtube.service.YoutubeQueryService;

import java.util.*;
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

    public CulinaryIngredientGroupResponse getIngredientStatistics(String culinaryName) {
        List<CulinaryIngredientResponse> resList = statisticsQueryService.getIngredientStatistics(culinaryName);
        List<CulinaryIngredientResponse> main = new ArrayList<>();
        List<CulinaryIngredientResponse> sub = new ArrayList<>();
        List<CulinaryIngredientResponse> other = new ArrayList<>();
        for (CulinaryIngredientResponse res : resList) {
            double percentage = res.percentage();
            if (percentage >= 80.0) {
                main.add(res);
            } else if (percentage >= 50.0) {
                sub.add(res);
            } else if (percentage >= 20.0) {
                other.add(res);
            }
        }
        return CulinaryIngredientGroupResponse.builder()
            .mainIngredients(main)
            .subIngredients(sub)
            .otherIngredients(other)
            .build();
    }

    public List<Long> getRandomVideoIngredientIds(String culinaryName) {
        return youtubeCommandService.getRandomVideoIngredientIds(culinaryName);
    }

    public VideoRecommendationResponseDto getRecommendedVideos(String culinaryName, List<Long> userIngredientIds) {
        Set<Long> userSet = new HashSet<>(userIngredientIds);
        List<YoutubeVideoMeta> metas = youtubeQueryService.findAllByCulinaryNameWithIngredients(culinaryName);

        if (metas.isEmpty()) {
            return new VideoRecommendationResponseDto(Collections.emptyList(), Collections.emptyList());
        }

        List<VideoResponseDto> matchVideos = extractMatchVideos(metas, userSet);

        List<VideoResponseDto> popularVideos = extractPopularVideos(metas);

        return new VideoRecommendationResponseDto(matchVideos, popularVideos);
    }

    private List<VideoResponseDto> extractMatchVideos(List<YoutubeVideoMeta> metas, Set<Long> userSet) {
        List<YoutubeVideoMeta> correct = new ArrayList<>();
        List<YoutubeVideoMeta> enough = new ArrayList<>();
        List<YoutubeVideoMeta> needed = new ArrayList<>();

        for (YoutubeVideoMeta meta : metas) {
            Set<Long> videoSet = meta.getIngredientIds();
            Set<Long> missingIds = new HashSet<>(videoSet);
            missingIds.removeAll(userSet);

            if (missingIds.isEmpty()) {
                Set<Long> surplusIds = new HashSet<>(userSet);
                surplusIds.removeAll(videoSet);
                if (surplusIds.isEmpty()) {
                    correct.add(meta);
                } else {
                    enough.add(meta);
                }
            } else if (missingIds.size() <= 3) {
                needed.add(meta);
            }
        }

        enough.sort(Comparator.comparingInt(m -> calculateDiffCount(m.getIngredientIds(), userSet)));
        needed.sort(Comparator.comparingInt(m -> calculateDiffCount(m.getIngredientIds(), userSet)));

        List<VideoResponseDto> result = new ArrayList<>();
        correct.forEach(m -> result.add(VideoResponseDto.from(m.getYoutubeVideo())));
        enough.forEach(m -> result.add(VideoResponseDto.from(m.getYoutubeVideo())));
        needed.forEach(m -> result.add(VideoResponseDto.from(m.getYoutubeVideo())));

        return result;
    }

    private int calculateDiffCount(Set<Long> videoSet, Set<Long> userSet) {
        Set<Long> diff = new HashSet<>(videoSet);
        diff.removeAll(userSet);
        if (diff.isEmpty()) {
            diff = new HashSet<>(userSet);
            diff.removeAll(videoSet);
        }
        return diff.size();
    }

    private List<VideoResponseDto> extractPopularVideos(List<YoutubeVideoMeta> metas) {
        return metas.stream()
            .map(YoutubeVideoMeta::getYoutubeVideo)
            .sorted((v1, v2) -> {
                Long count1 = v1.getViewCount() != null ? v1.getViewCount() : 0L;
                Long count2 = v2.getViewCount() != null ? v2.getViewCount() : 0L;
                return count2.compareTo(count1); // 내림차순 정렬
            })
            .limit(5)
            .map(VideoResponseDto::from)
            .toList();
    }

    public List<CulinaryRecommendationDto> getCulinaryRecommendations(List<Long> userIngredientIds) {
        Set<Long> userSet = new HashSet<>(userIngredientIds);
        List<YoutubeVideoMeta> allMetas = youtubeQueryService.findAllWithIngredients(userIngredientIds);
        Map<Long, String> ingredientNameMap = ingredientQueryService.findAll();

        Map<Long, List<YoutubeVideoMeta>> groupByCulinary = allMetas.stream()
            .collect(Collectors.groupingBy(meta -> meta.getCulinary().getId()));

        return groupByCulinary.values().stream()
            .map(metas -> createRecommendationDto(metas, userSet, ingredientNameMap))
            .filter(Objects::nonNull)
            .toList();
    }

    private CulinaryRecommendationDto createRecommendationDto(
        List<YoutubeVideoMeta> metas, Set<Long> userSet, Map<Long, String> nameMap) {

        MatchResult match = findBestMatch(metas, userSet);

        if (match == null) return null;

        String culinaryName = metas.get(0).getCulinary().getName();

        List<IngredientSimpleDto> ingredients = match.targetIngredientIds().stream()
            .map(id -> new IngredientSimpleDto(id, nameMap.getOrDefault(id, "알 수 없는 재료")))
            .toList();

        return CulinaryRecommendationDto.builder()
            .name(culinaryName)
            .status(match.status())
            .ingredients(ingredients)
            .build();
    }

    private MatchResult findBestMatch(List<YoutubeVideoMeta> metas, Set<Long> userSet) {
        String bestStatus = null;
        Set<Long> bestTargetIds = Collections.emptySet();
        int minMissingCount = Integer.MAX_VALUE;

        for (YoutubeVideoMeta meta : metas) {
            Set<Long> videoSet = meta.getIngredientIds();
            Set<Long> missingIds = new HashSet<>(videoSet);
            missingIds.removeAll(userSet);

            if (missingIds.isEmpty()) {
                Set<Long> surplusIds = new HashSet<>(userSet);
                surplusIds.removeAll(videoSet);
                if (surplusIds.isEmpty()) {
                    return new MatchResult("correct", Collections.emptySet());
                } else {
                    if (!"enough".equals(bestStatus)) {
                        bestStatus = "enough";
                        bestTargetIds = surplusIds;
                    }
                }
            } else if (missingIds.size() <= 3) {
                if (bestStatus == null || "needed".equals(bestStatus)) {
                    if (missingIds.size() < minMissingCount) {
                        bestStatus = "needed";
                        minMissingCount = missingIds.size();
                        bestTargetIds = missingIds;
                    }
                }
            }
        }

        if (bestStatus == null) return null;
        return new MatchResult(bestStatus, bestTargetIds);
    }

    record MatchResult(String status, Set<Long> targetIngredientIds) {}
}