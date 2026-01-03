package com.oman.domain.youtube.entity;

import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

// DB 저장 보다는 DTO

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeSearchResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // DB Primary Key

    @Column(nullable = false, length = 255)
    private String apiResultKind; // 결과의 종류 (예: "youtube#video", "youtube#channel", "youtube#playlist")

    @Column(nullable = false, length = 34) // Video, Channel, Playlist ID의 최대 길이
    private String apiResourceId; // 실제 자원의 API ID (videoId, channelId, playlistId 중 하나)

    @Column(nullable = false, length = 500)
    private String title; // 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 설명

    private OffsetDateTime publishedAt; // 게시일

    @Column(length = 24) // Channel ID 길이
    private String channelId; // 채널 ID

    @Column(length = 255)
    private String channelTitle; // 채널명

    @Column(columnDefinition = "TEXT")
    private String defaultThumbnailUrl; // 기본 썸네일 이미지 URL

    // --- 생성자 ---
    private YoutubeSearchResult(String apiResultKind, String apiResourceId, String title) {
        this.apiResultKind = apiResultKind;
        this.apiResourceId = apiResourceId;
        this.title = title;
    }

    public static YoutubeSearchResult from(String apiResultKind, String apiResourceId, String title) {
        return new YoutubeSearchResult(apiResultKind, apiResourceId, title);
    }

    public void updateDetails(String description, OffsetDateTime publishedAt, String channelId, String channelTitle, String defaultThumbnailUrl) {
        this.description = description;
        this.publishedAt = publishedAt;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.defaultThumbnailUrl = defaultThumbnailUrl;
    }
}