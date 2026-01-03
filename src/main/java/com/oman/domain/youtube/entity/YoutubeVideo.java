package com.oman.domain.youtube.entity;

import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeVideo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String apiVideoId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private OffsetDateTime publishedAt; // 게시일 (API에서 가져온 시간)

    // ManyToOne 관계: 여러 동영상이 하나의 채널에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private YoutubeChannel channel;

    @Column(length = 255)
    private String channelTitle; // 채널명 (캐싱용, YoutubeChannel.title과 중복될 수 있으나 API 응답에는 포함됨)

    @Column(length = 24)
    private String categoryId; // 카테고리 ID

    @Column(columnDefinition = "TEXT") // 태그 목록 (쉼표 구분 문자열 또는 JSON 형태로 저장)
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String defaultThumbnailUrl; // 기본 썸네일 이미지 URL

    // 통계 정보
    private Long viewCount; // 조회수
    private Long likeCount; // 좋아요 수
    private Long commentCount; // 댓글 수

    // 자막 정보
    @Column(columnDefinition = "TEXT")
    private String availableCaptionLanguages; // 사용 가능한 자막 언어 목록 (쉼표 구분 문자열 또는 JSON)

    // 제한 및 임베드 가능 여부
    @Column(length = 255)
    private String regionRestriction; // 지역 제한 정보

    private Boolean embeddable; // 임베드 가능 여부

    // 한 동영상에 여러 댓글 스레드가 달릴 수 있음
    @OneToMany(mappedBy = "video", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<YoutubeCommentThread> commentThreads = new ArrayList<>();

    // --- 생성자 ---
    private YoutubeVideo(String apiVideoId, String title, YoutubeChannel channel) {
        this.apiVideoId = apiVideoId;
        this.title = title;
        this.channel = channel;
    }

    public static YoutubeVideo from(String apiVideoId, String title, YoutubeChannel channel) {
        return new YoutubeVideo(apiVideoId, title, channel);
    }

    // 동영상 정보를 업데이트하는 메서드 예시
    public void updateDetails(String description, OffsetDateTime publishedAt, String categoryId, String tags, String defaultThumbnailUrl) {
        this.description = description;
        this.publishedAt = publishedAt;
        this.categoryId = categoryId;
        this.tags = tags;
        this.defaultThumbnailUrl = defaultThumbnailUrl;
    }

    public void updateStatistics(Long viewCount, Long likeCount, Long commentCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
