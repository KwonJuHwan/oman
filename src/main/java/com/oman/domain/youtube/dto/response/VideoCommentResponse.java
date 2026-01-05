package com.oman.domain.youtube.dto.response;

import com.google.api.services.youtube.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoCommentResponse {
    private String videoId;
    private String commentId;
    private String authorDisplayName;
    private String textDisplay;
    private String publishedAt;

    public static VideoCommentResponse from(String videoId, Comment topLevelComment) {
        if (topLevelComment == null || topLevelComment.getSnippet() == null) {
            return null; // 댓글 스니펫 자체가 없는 경우
        }
        return new VideoCommentResponse(
            videoId,
            topLevelComment.getId(),
            Optional.ofNullable(topLevelComment.getSnippet().getAuthorDisplayName()).orElse("익명"),
            Optional.ofNullable(topLevelComment.getSnippet().getTextDisplay()).orElse("내용 없음"),
            Optional.ofNullable(topLevelComment.getSnippet().getPublishedAt())
                .map(com.google.api.client.util.DateTime::toStringRfc3339)
                .orElse(null)
        );
    }
}
