package com.oman.domain.youtube.controller;

import com.oman.domain.youtube.dto.response.YoutubeVideoResponse;
import com.oman.domain.youtube.service.YoutubeService;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<YoutubeVideoResponse> searchYoutubeVideos(@RequestParam String query) {
        YoutubeVideoResponse videoResponses = youtubeService.processAndSaveRecipeVideos(query);
        return ResponseEntity.ok(videoResponses);
    }

    @GetMapping("/search/summary/export-txt")
    public ResponseEntity<String> exportSummaryToText(@RequestParam String query) {
        Path savedFilePath = youtubeService.saveDescriptionsForLabeling(query);

        if (savedFilePath != null) {
            String message = "YouTube 동영상 요약 정보가 파일로 성공적으로 저장되었습니다: " + savedFilePath.toAbsolutePath();
            return ResponseEntity.ok(message);
        } else {
            String errorMessage = "YouTube 동영상 요약 정보 파일 저장에 실패했거나, 해당 검색어에 대한 동영상이 없습니다.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}
