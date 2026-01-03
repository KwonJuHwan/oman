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
public class YoutubeVideoSearchResponse {
    private String apiVideoId;
    private String title;
    private String description;
    private String publishedAt; // ISO 8601 형식의 문자열
    private String channelId;
    private String channelTitle;
    private String defaultThumbnailUrl; // 기본 썸네일 URL

    // API 응답 데이터를 DTO로 변환하는 팩토리 메서드
    public static YoutubeVideoSearchResponse from(SearchResult searchResult) {
        String videoId = searchResult.getId().getVideoId();
        String title = Optional.ofNullable(searchResult.getSnippet().getTitle()).orElse("제목 없음");
        String description = Optional.ofNullable(searchResult.getSnippet().getDescription()).orElse("");
        String publishedAt = Optional.ofNullable(searchResult.getSnippet().getPublishedAt())
            .map(com.google.api.client.util.DateTime::toStringRfc3339) // DateTime -> ISO 8601 String
            .orElse(null);
        String channelId = searchResult.getSnippet().getChannelId();
        String channelTitle = searchResult.getSnippet().getChannelTitle();

        // 썸네일 정보 가져오기 (고해상도 썸네일 우선)
        String defaultThumbnailUrl = Optional.ofNullable(searchResult.getSnippet().getThumbnails())
            .map(ThumbnailDetails::getHigh) // high 썸네일 시도
            .map(Thumbnail::getUrl)
            .orElseGet(() -> Optional.ofNullable(searchResult.getSnippet().getThumbnails())
                .map(ThumbnailDetails::getDefault) // default 썸네일 시도
                .map(Thumbnail::getUrl)
                .orElse(null)); // 둘 다 없으면 null

        return new YoutubeVideoSearchResponse(
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
