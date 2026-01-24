package com.oman.domain.batch.processor;

import com.oman.domain.culinary.entity.Culinary;
import com.oman.domain.statistic.entity.IngredientStatistic;
import com.oman.domain.statistic.service.IngredientStatisticProcessService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class IngredientStatisticProcessor implements
    ItemProcessor<Culinary, List<IngredientStatistic>> {
    private final IngredientStatisticProcessService ingredientStatisticProcessService;

    @Override
    public List<IngredientStatistic> process(Culinary culinary) {
        return ingredientStatisticProcessService.createStatistics(culinary);
    }
}
