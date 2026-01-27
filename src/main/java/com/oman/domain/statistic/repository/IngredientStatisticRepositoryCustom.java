package com.oman.domain.statistic.repository;

import com.oman.domain.statistic.entity.IngredientStatistic;
import java.util.List;

public interface IngredientStatisticRepositoryCustom {
    List<IngredientStatistic> findAllByCulinaryName(String culinaryName);
}
