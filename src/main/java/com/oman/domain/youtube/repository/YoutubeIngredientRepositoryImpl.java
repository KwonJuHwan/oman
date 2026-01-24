package com.oman.domain.youtube.repository;


import com.oman.domain.statistic.dto.IngredientCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import static com.oman.domain.youtube.entity.QYoutubeVideo.youtubeVideo;
import static com.oman.domain.youtube.entity.QYoutubeIngredient.youtubeIngredient;

@RequiredArgsConstructor
public class YoutubeIngredientRepositoryImpl implements YoutubeIngredientRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    @Override
    public List<IngredientCountDto> countIngredientsByCulinaryId(Long culinaryId) {
        return queryFactory
            .select(Projections.constructor(IngredientCountDto.class,
                youtubeIngredient.ingredient.id,
                youtubeVideo.id.countDistinct()
            ))
            .from(youtubeIngredient)
            .join(youtubeIngredient.youtubeVideo, youtubeVideo)
            .where(youtubeVideo.culinary.id.eq(culinaryId))
            .groupBy(youtubeIngredient.ingredient.id)
            .fetch();
    }

    @Override
    public long countTotalVideosByCulinaryId(Long culinaryId) {
        Long count = queryFactory
            .select(youtubeVideo.count())
            .from(youtubeVideo)
            .where(youtubeVideo.culinary.id.eq(culinaryId))
            .fetchOne();

        return count != null ? count : 0L;
    }
}
