package com.oman.domain.youtube.entity;

import com.oman.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;


// 댓글 내용을 담는 엔티티
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // DB Primary Key

    @Column(nullable = false, unique = true, length = 24) // YouTube Comment ID
    private String apiCommentId; // YouTube API의 Comment ID

    @Column(nullable = false, length = 255)
    private String authorDisplayName; // 작성자 이름

    @Column(columnDefinition = "TEXT")
    private String authorProfileImageUrl; // 작성자 프로필 이미지 URL

    @Column(columnDefinition = "TEXT")
    private String textDisplay; // 댓글 내용 (HTML 태그 제거 등, 표시용)

    @Column(columnDefinition = "TEXT")
    private String textOriginal; // 원본 댓글 내용 (HTML 포함 가능)

    private OffsetDateTime publishedAt; // 게시일


    private Long likeCount; // 좋아요 수

    // Self-referencing: 답글인 경우 부모 댓글을 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private YoutubeComment parentComment;

    // ManyToOne 관계: 댓글은 하나의 댓글 스레드에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_thread_id") // comment_thread_id는 답글인 경우만 사용
    private YoutubeCommentThread commentThread; // 최상위 댓글의 경우 null 또는 별도 관리

    // --- 생성자 ---
    private YoutubeComment(String apiCommentId, String authorDisplayName, String textDisplay) {
        this.apiCommentId = apiCommentId;
        this.authorDisplayName = authorDisplayName;
        this.textDisplay = textDisplay;
    }

    public static YoutubeComment from(String apiCommentId, String authorDisplayName, String textDisplay) {
        return new YoutubeComment(apiCommentId, authorDisplayName, textDisplay);
    }

    // 답글 추가 시 사용 (부모 댓글 설정)
    public static YoutubeComment fromReply(String apiCommentId, String authorDisplayName, String textDisplay, YoutubeComment parentComment) {
        YoutubeComment comment = new YoutubeComment(apiCommentId, authorDisplayName, textDisplay);
        comment.parentComment = parentComment;
        return comment;
    }

    public void updateDetails(String authorProfileImageUrl, String textOriginal, OffsetDateTime publishedAt, Long likeCount) {
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.textOriginal = textOriginal;
        this.publishedAt = publishedAt;
        this.likeCount = likeCount;
    }
}
