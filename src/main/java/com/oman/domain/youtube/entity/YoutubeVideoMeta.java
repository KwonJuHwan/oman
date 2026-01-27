package com.oman.domain.youtube.entity;

import com.oman.domain.culinary.entity.Culinary;
import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class YoutubeVideoMeta extends BaseTimeEntity {

    @Id
    private Long youtubeVideoId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youtube_video_id")
    private YoutubeVideo youtubeVideo;

    @Column(nullable = false)
    private Integer totalIngredientCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culinary_id")
    private Culinary culinary;

    @ElementCollection
    @CollectionTable(name = "youtube_video_ingredient_ids",
        joinColumns = @JoinColumn(name = "youtube_video_id"))
    @Column(name = "ingredient_id")
    private Set<Long> ingredientIds = new HashSet<>();

    public static YoutubeVideoMeta of(YoutubeVideo video, Set<Long> ids) {
        return YoutubeVideoMeta.builder()
            .youtubeVideo(video)
            .culinary(video.getCulinary())
            .ingredientIds(ids)
            .totalIngredientCount(ids.size())
            .build();
    }
}