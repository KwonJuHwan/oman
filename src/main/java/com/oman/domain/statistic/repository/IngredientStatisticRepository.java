package com.oman.domain.statistic.repository;

import com.oman.domain.statistic.entity.IngredientStatistic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientStatisticRepository extends JpaRepository<IngredientStatistic,Long> {
    // 기존에 해당 요리-재료 조합의 통계가 있는지 확인 (업데이트용)
    Optional<IngredientStatistic> findByCulinaryIdAndIngredientId(Long culinaryId, Long ingredientId);

    List<IngredientStatistic> findAllByCulinaryIdIn(List<Long> culinaryIds);
}
