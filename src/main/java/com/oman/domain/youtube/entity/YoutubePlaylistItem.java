package com.oman.domain.youtube.entity;

import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubePlaylistItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String apiPlaylistItemId;

    // ManyToOne 관계: 여러 항목이 하나의 재생목록에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false) // foreign key 설정
    private YoutubePlaylist playlist;

    // ManyToOne 관계: 하나의 재생목록 항목이 하나의 동영상을 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false) // foreign key 설정
    private YoutubeVideo video; // 재생목록 항목으로 포함된 동영상

    @Column(nullable = false, length = 500)
    private String title; // 동영상 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 동영상 설명

    private OffsetDateTime publishedAt; // 재생목록에 추가된 날짜

    @Column(columnDefinition = "TEXT")
    private String defaultThumbnailUrl; // 동영상 썸네일 이미지 URL

    // --- 생성자 ---
    private YoutubePlaylistItem(String apiPlaylistItemId, YoutubePlaylist playlist, YoutubeVideo video, String title) {
        this.apiPlaylistItemId = apiPlaylistItemId;
        this.playlist = playlist;
        this.video = video;
        this.title = title;
    }

    public static YoutubePlaylistItem from(String apiPlaylistItemId, YoutubePlaylist playlist, YoutubeVideo video, String title) {
        return new YoutubePlaylistItem(apiPlaylistItemId, playlist, video, title);
    }

    public void updateDetails(String description, OffsetDateTime publishedAt, String defaultThumbnailUrl) {
        this.description = description;
        this.publishedAt = publishedAt;
        this.defaultThumbnailUrl = defaultThumbnailUrl;
    }
}