package com.oman.domain.youtube.entity;

import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeCommentThread extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // DB Primary Key

    @Column(nullable = false, unique = true, length = 24) // YouTube CommentThread ID
    private String apiCommentThreadId; // YouTube API의 CommentThread ID

    // ManyToOne 관계: 댓글 스레드는 하나의 동영상에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private YoutubeVideo video;

    // OneToOne 관계로 최상위 댓글 참조
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "top_level_comment_id")
    private YoutubeComment topLevelComment;

    // OneToMany 관계: 이 스레드의 답글 목록 (필요에 따라 분리/관리)
    // @OneToMany(mappedBy = "commentThread", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    // private final List<YoutubeComment> replies = new ArrayList<>(); // 이 방법은 답글이 매우 많을 때 성능 문제 발생 가능성이 있어, 필요한 경우에만 조회하도록 별도 리포지토리 메서드 사용 권장

    // --- 생성자 ---
    private YoutubeCommentThread(String apiCommentThreadId, YoutubeVideo video, YoutubeComment topLevelComment) {
        this.apiCommentThreadId = apiCommentThreadId;
        this.video = video;
        this.topLevelComment = topLevelComment;
    }

    public static YoutubeCommentThread from(String apiCommentThreadId, YoutubeVideo video, YoutubeComment topLevelComment) {
        return new YoutubeCommentThread(apiCommentThreadId, video, topLevelComment);
    }
}
