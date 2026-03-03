package com.oman.domain.recipe.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VideoRecommendationResponseDto(
    @JsonProperty("match_videos")
    List<VideoResponseDto> matchVideos,

    @JsonProperty("popular_videos")
    List<VideoResponseDto> popularVideos
) {}
