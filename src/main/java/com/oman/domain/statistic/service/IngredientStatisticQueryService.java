package com.oman.domain.statistic.service;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.culinary.repository.IngredientRepository;
import com.oman.domain.culinary.service.CulinaryQueryService;
import com.oman.domain.culinary.service.IngredientQueryService;
import com.oman.domain.statistic.dto.CulinaryIngredientResponse;
import com.oman.domain.statistic.dto.IngredientCountDto;
import com.oman.domain.statistic.repository.IngredientStatisticRepository;
import com.oman.domain.youtube.repository.YoutubeIngredientRepository;
import com.oman.domain.youtube.service.YoutubeQueryService;
import java.util.List;
import java.util.Optional;
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

    public Optional<Ingredient> findIngredientById(Long id) {
        return ingredientQueryService.findIngredientById(id);
    }

    public List<CulinaryIngredientResponse> getIngredientStatistics(String culinaryName) {
        // 검색한 요리가 없을때 처리 필요
        if (!culinaryQueryService.culinaryExistByName(culinaryName)) {
            throw new IllegalArgumentException("해당 요리를 찾을 수 없습니다: " + culinaryName);
        }

        return ingredientStatisticRepository.findAllByCulinaryName(culinaryName).stream()
            .map(CulinaryIngredientResponse::from)
            .toList();
    }

}

