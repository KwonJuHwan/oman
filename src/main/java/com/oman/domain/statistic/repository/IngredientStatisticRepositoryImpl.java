package com.oman.domain.statistic.repository;

import com.oman.domain.statistic.entity.IngredientStatistic;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.oman.domain.culinary.entity.QCulinary.culinary;
import static com.oman.domain.culinary.entity.QIngredient.ingredient;
import static com.oman.domain.statistic.entity.QIngredientStatistic.ingredientStatistic;

@RequiredArgsConstructor
public class IngredientStatisticRepositoryImpl implements IngredientStatisticRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<IngredientStatistic> findAllByCulinaryName(String culinaryName) {
        return jpaQueryFactory
            .selectFrom(ingredientStatistic)
            // N+1 문제 해결을 위한 Fetch Join
            .join(ingredientStatistic.ingredient, ingredient).fetchJoin()
            .join(ingredientStatistic.culinary, culinary)
            .where(culinary.name.eq(culinaryName))
            .orderBy(ingredientStatistic.percentage.desc())
            .fetch();
    }
}
