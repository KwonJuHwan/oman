package com.oman.domain.statistic.service;

import com.oman.domain.culinary.entity.Culinary;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.statistic.dto.IngredientCountDto;
import com.oman.domain.statistic.entity.IngredientStatistic;
import com.oman.global.error.ErrorCode;
import com.oman.global.error.exception.StatisticException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientStatisticProcessService {
    private final IngredientStatisticQueryService queryService;

    public List<IngredientStatistic> createStatistics(Culinary culinary) {
        Long culinaryId = culinary.getId();
        long totalVideoCount = queryService.getTotalVideoCount(culinaryId);

        if (totalVideoCount == 0) {
            throw new StatisticException(ErrorCode.STATISTIC_GENERATION_FAILED);
        }

        List<IngredientCountDto> counts = queryService.getIngredientCountsForCulinary(culinaryId);

        return counts.stream()
            .map(dto -> {
                Ingredient ingredientOpt = queryService.findIngredientById(dto.ingredientId());

                double percentage = Math.round(((double) dto.count() / totalVideoCount * 100) * 100.0) / 100.0;

                return IngredientStatistic.builder()
                    .culinary(culinary)
                    .ingredient(ingredientOpt)
                    .totalVideoCount((int) totalVideoCount)
                    .ingredientIncludedVideoCount(dto.count().intValue())
                    .percentage(percentage)
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
