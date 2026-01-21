package com.oman.domain.youtube.dto.response;

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
    private Map<String, String> videoDescriptions;
}
