package com.oman.domain.youtube.dto.response;


import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VideoSearchResponse {
    private String apiVideoId;
    private String title;
    private String description;
    private String publishedAt;
    private String channelId;
    private String channelTitle;
    private String defaultThumbnailUrl;

    public static VideoSearchResponse from(SearchResult searchResult) {
        String videoId = searchResult.getId().getVideoId();
        String title = Optional.ofNullable(searchResult.getSnippet().getTitle()).orElse("제목 없음");
        String description = Optional.ofNullable(searchResult.getSnippet().getDescription()).orElse("");
        String publishedAt = Optional.ofNullable(searchResult.getSnippet().getPublishedAt())
            .map(com.google.api.client.util.DateTime::toStringRfc3339)
            .orElse(null);
        String channelId = searchResult.getSnippet().getChannelId();
        String channelTitle = searchResult.getSnippet().getChannelTitle();

        String defaultThumbnailUrl = Optional.ofNullable(searchResult.getSnippet().getThumbnails())
            .map(ThumbnailDetails::getHigh)
            .map(Thumbnail::getUrl)
            .orElseGet(() -> Optional.ofNullable(searchResult.getSnippet().getThumbnails())
                .map(ThumbnailDetails::getDefault)
                .map(Thumbnail::getUrl)
                .orElse(null));

        return new VideoSearchResponse(
            videoId,
            title,
            description,
            publishedAt,
            channelId,
            channelTitle,
            defaultThumbnailUrl
        );
    }
}
