package com.oman.domain.statistic.service;

import com.oman.domain.statistic.entity.IngredientStatistic;
import com.oman.domain.statistic.repository.IngredientStatisticRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientStatisticCommandService {

    private final IngredientStatisticRepository statisticRepository;

    public void upsertStatistics(List<IngredientStatistic> newStats) {
        if (newStats.isEmpty()) return;

        List<Long> culinaryIds = newStats.stream()
            .map(s -> s.getCulinary().getId())
            .distinct()
            .toList();

        List<IngredientStatistic> existingStats = statisticRepository.findAllByCulinaryIdIn(culinaryIds);

        Map<String, IngredientStatistic> existingMap = existingStats.stream()
            .collect(Collectors.toMap(
                s -> s.getCulinary().getId() + "-" + s.getIngredient().getId(),
                s -> s
            ));

        List<IngredientStatistic> toSave = new ArrayList<>();

        for (IngredientStatistic ns : newStats) {
            String key = ns.getCulinary().getId() + "-" + ns.getIngredient().getId();

            if (existingMap.containsKey(key)) {
                IngredientStatistic existing = existingMap.get(key);
                existing.updateStatistics(
                    ns.getTotalVideoCount(),
                    ns.getIngredientIncludedVideoCount(),
                    ns.getPercentage()
                );
                toSave.add(existing);
            } else {
                toSave.add(ns);
            }
        }

        statisticRepository.saveAll(toSave);
    }
}