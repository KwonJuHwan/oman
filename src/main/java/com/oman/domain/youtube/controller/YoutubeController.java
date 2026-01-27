package com.oman.domain.youtube.controller;

import com.oman.domain.youtube.service.YoutubeProcessService;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {
    private final YoutubeProcessService youtubeService;

    /**
     * 유튜브 레시피 영상 수집 및 데이터화
     * @param recipeName 검색할 요리 이름 (예: 김치찌개)
     * @param reInference 강제 재추론 여부 (기존 재료/메타데이터 삭제 후 재생성)
     */
    @PostMapping("/recipes/{recipeName}/process")
    public ResponseEntity<String> processYoutubeRecipes(
        @PathVariable String recipeName,
        @RequestParam(value = "reInference", defaultValue = "false") boolean reInference) {
        String result = youtubeService.processAndSaveRecipeVideos(recipeName, reInference);
        return ResponseEntity.ok(result);
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
