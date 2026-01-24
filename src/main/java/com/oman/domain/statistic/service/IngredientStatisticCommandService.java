package com.oman.domain.statistic.service;

import com.oman.domain.culinary.repository.CulinaryRepository;
import com.oman.domain.culinary.repository.IngredientRepository;
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

        // 1. 현재 처리하려는 요리(Culinary)들의 ID 목록 추출
        List<Long> culinaryIds = newStats.stream()
            .map(s -> s.getCulinary().getId())
            .distinct()
            .toList();

        // 2. DB에서 기존 통계 데이터를 한 번에 조회
        List<IngredientStatistic> existingStats = statisticRepository.findAllByCulinaryIdIn(culinaryIds);

        // 3. 비교를 위해 Map으로 변환
        Map<String, IngredientStatistic> existingMap = existingStats.stream()
            .collect(Collectors.toMap(
                s -> s.getCulinary().getId() + "-" + s.getIngredient().getId(),
                s -> s
            ));

        List<IngredientStatistic> toSave = new ArrayList<>();

        for (IngredientStatistic ns : newStats) {
            String key = ns.getCulinary().getId() + "-" + ns.getIngredient().getId();

            if (existingMap.containsKey(key)) {
                // [Update] 이미 있다면 기존 객체의 값 업데이트
                IngredientStatistic existing = existingMap.get(key);
                existing.updateStatistics(
                    ns.getTotalVideoCount(),
                    ns.getIngredientIncludedVideoCount(),
                    ns.getPercentage()
                );
                toSave.add(existing);
            } else {
                // [Insert] 없다면 새로 생성된 객체 추가
                toSave.add(ns);
            }
        }

        statisticRepository.saveAll(toSave);
    }
}