package com.oman.domain.statistic.repository;

import com.oman.domain.statistic.entity.IngredientStatistic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientStatisticRepository extends JpaRepository<IngredientStatistic,Long>,IngredientStatisticRepositoryCustom {
    Optional<IngredientStatistic> findByCulinaryIdAndIngredientId(Long culinaryId, Long ingredientId);

    List<IngredientStatistic> findAllByCulinaryIdIn(List<Long> culinaryIds);
}
