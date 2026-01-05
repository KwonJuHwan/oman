package com.oman.domain.youtube.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class VideoWithCommentResponse {
    // 동영상 자체의 정보 (YoutubeVideoSearchResponse 필드들을 직접 여기에 포함)
    private String apiVideoId;
    private String title;
    private String description;
    private String publishedAt;
    private String channelId;
    private String channelTitle;
    private String defaultThumbnailUrl;


    private VideoCommentResponse topComment;

    public static VideoWithCommentResponse from(
        VideoSearchResponse videoInfo,
        VideoCommentResponse topComment
    ) {
        return new VideoWithCommentResponse(
            videoInfo.getApiVideoId(),
            videoInfo.getTitle(),
            videoInfo.getDescription(),
            videoInfo.getPublishedAt(),
            videoInfo.getChannelId(),
            videoInfo.getChannelTitle(),
            videoInfo.getDefaultThumbnailUrl(),
            topComment
        );
    }
}
