package com.oman.domain.youtube.repository;

import static com.oman.domain.culinary.entity.QCulinary.culinary;
import static com.oman.domain.youtube.entity.QYoutubeVideo.youtubeVideo;
import static com.oman.domain.youtube.entity.QYoutubeVideoMeta.youtubeVideoMeta;

import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YoutubeVideoMetaRepositoryImpl implements YoutubeVideoMetaRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<YoutubeVideoMeta> findAllWithIngredients(List<Long> userIngredientIds) {

        return queryFactory
            .selectFrom(youtubeVideoMeta)
            .join(youtubeVideoMeta.youtubeVideo, youtubeVideo).fetchJoin()
            .join(youtubeVideo.culinary, culinary).fetchJoin()
            // 사용자가 가진 재료 중 하나라도 포함된 영상만 필터링
            .where(youtubeVideoMeta.ingredientIds.any().in(userIngredientIds))
            .distinct()
            .fetch();
    }

    @Override
    public List<YoutubeVideoMeta> findAllByCulinaryNameWithIngredients(String culinaryName) {
        return queryFactory
            .selectFrom(youtubeVideoMeta)
            .join(youtubeVideoMeta.youtubeVideo, youtubeVideo).fetchJoin()
            .join(youtubeVideo.culinary, culinary).fetchJoin()
            .leftJoin(youtubeVideoMeta.ingredientIds).fetchJoin()
            .where(culinary.name.eq(culinaryName))
            .distinct()
            .fetch();
    }
}
