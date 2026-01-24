package com.oman.domain.statistic.service;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.culinary.repository.IngredientRepository;
import com.oman.domain.statistic.dto.IngredientCountDto;
import com.oman.domain.youtube.repository.YoutubeIngredientRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientStatisticQueryService {

    private final YoutubeIngredientRepository youtubeIngredientRepository;
    private final IngredientRepository ingredientRepository;

    public List<IngredientCountDto> getIngredientCountsForCulinary(Long culinaryId) {
        return youtubeIngredientRepository.countIngredientsByCulinaryId(culinaryId);
    }

    public long getTotalVideoCount(Long culinaryId) {
        return youtubeIngredientRepository.countTotalVideosByCulinaryId(culinaryId);
    }

    public Optional<Ingredient> findIngredientById(Long id) {
        return ingredientRepository.findById(id);
    }

}

