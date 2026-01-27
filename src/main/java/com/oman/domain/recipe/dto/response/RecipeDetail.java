package com.oman.domain.recipe.dto.response;

import com.oman.domain.youtube.entity.YoutubeVideo;
import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import java.util.List;

public record RecipeDetail(
    Long videoId,
    String apiVideoId,
    String videoTitle,
    String thumbnailUrl,
    List<String> diffIngredientNames, // 빼거나 더해야 할 재료 이름들
    int diffCount
) {

    public static RecipeDetail of(YoutubeVideoMeta meta, List<String> diffNames, int count) {
        YoutubeVideo video = meta.getYoutubeVideo();

        return new RecipeDetail(
            video.getId(),
            video.getApiVideoId(),
            video.getTitle(),
            video.getThumbnailUrl(),
            diffNames,
            count
        );
    }
}
