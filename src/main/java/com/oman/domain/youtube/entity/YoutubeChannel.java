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
public class YoutubeChannel extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 24)
    private String apiChannelId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description; // 채널 설명

    private OffsetDateTime publishedAt; // 채널 생성일 (API에서 가져온 시간)

    private String country;

    @Column(columnDefinition = "TEXT")
    private String defaultThumbnailUrl;

    // 통계 정보 (nullable = true로 설정하여 없을 수도 있음을 표현)
    private Long subscriberCount; // 구독자 수
    private Long viewCount; // 총 조회수
    private Long videoCount; // 총 동영상 개수

    private String brandingThemeColor; // 채널 테마 색상

    // ManyToMany 또는 별도 엔티티로 분리할 수도 있지만, 러프하게는 List<String> 형태로 매핑 고려
    // @ElementCollection 등으로 매핑 필요. 여기서는 일단 컬럼으로 정의하고 직렬화/역직렬화 고려
    @Column(columnDefinition = "TEXT") // List<String>을 JSON 또는 쉼표 구분 문자열로 저장하기 위해
    private String featuredChannelsUrls; // 추천 채널(외부 링크) 목록

    @Column(columnDefinition = "TEXT")
    private String channelSectionTitles; // 채널 섹션 제목 목록

    // 한 채널이 여러 동영상을 가질 수 있음
    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<YoutubeVideo> videos = new ArrayList<>();

    // 한 채널이 여러 재생목록을 가질 수 있음
    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<YoutubePlaylist> playlists = new ArrayList<>();

    // --- 생성자 ---
    // (apiChannelId, title) 등 핵심 필드를 받는 생성자 예시
    private YoutubeChannel(String apiChannelId, String title) {
        this.apiChannelId = apiChannelId;
        this.title = title;
    }

    public static YoutubeChannel from(String apiChannelId, String title) {
        return new YoutubeChannel(apiChannelId, title);
    }

    // 통계 정보 등을 업데이트하는 메서드 예시
    public void updateStatistics(Long subscriberCount, Long viewCount, Long videoCount) {
        this.subscriberCount = subscriberCount;
        this.viewCount = viewCount;
        this.videoCount = videoCount;
    }

    // 다른 필드를 업데이트하는 메서드도 추가할 수 있습니다.
}
