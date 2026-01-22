package com.oman.domain.youtube.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoutubeVideoResponse {
    private String searchedRecipeName;

    @JsonProperty("videoDescriptions")
    private Map<String, String> videoDescriptions;
}
