package com.oman.domain.youtube.controller;

import com.oman.domain.youtube.dto.response.VideoSearchResponse;
import com.oman.domain.youtube.dto.response.VideoWithCommentResponse;
import com.oman.domain.youtube.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {
    private final YoutubeService youtubeService;

    /**
     * 검색어를 기반으로 상위 10개의 유튜브 동영상 정보와 첫 댓글 조회
     * @param query 검색어
     * @return 상위 10개 동영상의 정보 리스트
     */
    @GetMapping("/search/videos")
    public ResponseEntity<List<VideoWithCommentResponse>> searchYoutubeVideos(@RequestParam String query) {
        List<VideoWithCommentResponse> videoResponses = youtubeService.searchVideosInfo(query);
        return ResponseEntity.ok(videoResponses);
    }
}
