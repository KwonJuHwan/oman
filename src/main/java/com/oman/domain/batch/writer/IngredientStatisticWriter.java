package com.oman.domain.batch.writer;

import com.oman.domain.statistic.entity.IngredientStatistic;
import com.oman.domain.statistic.service.IngredientStatisticCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientStatisticWriter implements ItemWriter<List<IngredientStatistic>> {
    private final IngredientStatisticCommandService commandService;

    @Override
    public void write(Chunk<? extends List<IngredientStatistic>> chunk) {
        List<IngredientStatistic> allStats = chunk.getItems().stream()
            .flatMap(List::stream)
            .toList();

        commandService.upsertStatistics(allStats);
    }
}