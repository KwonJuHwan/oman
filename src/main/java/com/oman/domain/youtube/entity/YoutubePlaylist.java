package com.oman.domain.youtube.entity;

import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubePlaylist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 34) // YouTube Playlist ID는 고유하며 최대 34자
    private String apiPlaylistId; // YouTube API의 Playlist ID

    @Column(nullable = false, length = 500)
    private String title; // 재생목록 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 재생목록 설명

    private OffsetDateTime publishedAt; // 생성 날짜 (API에서 가져온 시간)

    // ManyToOne 관계: 여러 재생목록이 하나의 채널에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false) // foreign key 설정
    private YoutubeChannel channel;

    @Column(length = 255)
    private String channelTitle; // 채널명 (캐싱용)

    @Column(columnDefinition = "TEXT")
    private String defaultThumbnailUrl; // 기본 썸네일 이미지 URL

    private Long itemCount; // 재생목록 내 동영상 개수

    // 하나의 재생목록에 여러 항목이 포함됨
    @OneToMany(mappedBy = "playlist", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<YoutubePlaylistItem> items = new ArrayList<>();

    // --- 생성자 ---
    private YoutubePlaylist(String apiPlaylistId, String title, YoutubeChannel channel) {
        this.apiPlaylistId = apiPlaylistId;
        this.title = title;
        this.channel = channel;
    }

    public static YoutubePlaylist from(String apiPlaylistId, String title, YoutubeChannel channel) {
        return new YoutubePlaylist(apiPlaylistId, title, channel);
    }

    public void updateDetails(String description, OffsetDateTime publishedAt, String defaultThumbnailUrl, Long itemCount) {
        this.description = description;
        this.publishedAt = publishedAt;
        this.defaultThumbnailUrl = defaultThumbnailUrl;
        this.itemCount = itemCount;
    }
}
