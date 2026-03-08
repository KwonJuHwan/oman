package com.oman.domain.statistic.service;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.culinary.service.CulinaryQueryService;
import com.oman.domain.culinary.service.IngredientQueryService;
import com.oman.domain.statistic.dto.CulinaryIngredientResponse;
import com.oman.domain.statistic.dto.IngredientCountDto;
import com.oman.domain.statistic.entity.IngredientStatistic;
import com.oman.domain.statistic.repository.IngredientStatisticRepository;
import com.oman.domain.youtube.service.YoutubeQueryService;
import com.oman.global.error.ErrorCode;
import com.oman.global.error.exception.CulinaryException;
import com.oman.global.error.exception.StatisticException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientStatisticQueryService {

    private final YoutubeQueryService youtubeQueryService;
    private final IngredientQueryService ingredientQueryService;
    private final IngredientStatisticRepository ingredientStatisticRepository;
    private final CulinaryQueryService culinaryQueryService;


    public List<IngredientCountDto> getIngredientCountsForCulinary(Long culinaryId) {
        return youtubeQueryService.getIngredientCountsForCulinary(culinaryId);
    }

    public long getTotalVideoCount(Long culinaryId) {
        return youtubeQueryService.getTotalVideoCount(culinaryId);
    }

    public Ingredient findIngredientById(Long id) {
        return ingredientQueryService.findIngredientById(id);
    }

    public List<CulinaryIngredientResponse> getIngredientStatistics(String culinaryName) {
        if (!culinaryQueryService.culinaryExistByName(culinaryName)) {
            throw new CulinaryException(ErrorCode.CULINARY_NOT_FOUND);
        }
        List<IngredientStatistic> statistics = ingredientStatisticRepository.findAllByCulinaryName(culinaryName);
        if (statistics.isEmpty()) {
            throw new StatisticException(ErrorCode.STATISTIC_NOT_FOUND);
        }
        return statistics.stream()
            .map(CulinaryIngredientResponse::from)
            .toList();
    }

}

