package com.oman.domain.statistic.service;

import com.oman.domain.culinary.entity.Culinary;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.statistic.dto.IngredientCountDto;
import com.oman.domain.statistic.entity.IngredientStatistic;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientStatisticProcessService {
    private final IngredientStatisticQueryService queryService;

    /**
     * 하나의 요리(Culinary)에 대한 모든 재료 통계를 산출하고 저장
     */
    public List<IngredientStatistic> createStatistics(Culinary culinary) {
        Long culinaryId = culinary.getId();
        long totalVideoCount = queryService.getTotalVideoCount(culinaryId);

        if (totalVideoCount == 0) return Collections.emptyList();

        List<IngredientCountDto> counts = queryService.getIngredientCountsForCulinary(culinaryId);

        return counts.stream()
            .map(dto -> {
                Optional<Ingredient> ingredientOpt = queryService.findIngredientById(dto.ingredientId());

                if (ingredientOpt.isEmpty()) {
                    log.warn("통계 계산 중 재료를 찾을 수 없음: ID {}", dto.ingredientId());
                    return null;
                }

                double percentage = Math.round(((double) dto.count() / totalVideoCount * 100) * 100.0) / 100.0;

                return IngredientStatistic.builder()
                    .culinary(culinary)
                    .ingredient(ingredientOpt.get())
                    .totalVideoCount((int) totalVideoCount)
                    .ingredientIncludedVideoCount(dto.count().intValue())
                    .percentage(percentage)
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
