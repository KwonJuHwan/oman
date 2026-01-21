package com.oman.domain.youtube.entity;

import com.oman.domain.culinary.entity.Culinary;
import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class YoutubeVideo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String apiVideoId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 255)
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private YoutubeChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culinary_id", nullable = false)
    private Culinary culinary;

    @OneToMany(mappedBy = "youtubeVideo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<YoutubeIngredient> videoIngredients = new ArrayList<>();

    // 통계 정보
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;


    public void updateStatistics(Long viewCount, Long likeCount, Long commentCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

}
