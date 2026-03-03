package com.oman.domain.recipe.dto.response;

import com.oman.domain.youtube.entity.YoutubeVideo;

public record VideoResponseDto(
    Long id,
    String thumbnail,
    String title,
    String channel,
    String url,
    Long count
) {
    // Entity -> DTO 변환을 담당하는 정적 메서드
    public static VideoResponseDto from(YoutubeVideo video) {
        return new VideoResponseDto(
            video.getId(),
            video.getThumbnailUrl(),
            video.getTitle(),
            video.getChannel().getTitle(),
            "https://www.youtube.com/watch?v=" + video.getApiVideoId(),
            video.getViewCount() != null ? video.getViewCount() : 0L
        );
    }
}