package com.oman.domain.youtube.file;

import org.springframework.beans.factory.annotation.Value;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.oman.domain.fastapi.dto.ExtractedIngredient;
import com.oman.domain.fastapi.dto.InferenceResultForVideo;
import com.oman.domain.youtube.entity.YoutubeVideo;
import com.oman.domain.youtube.service.YoutubeApiClient;
import com.oman.domain.youtube.service.YoutubeCommandService;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoLabelingImportService {

    private final DoccanoLocalParser doccanoLocalParser;
    private final YoutubeApiClient youtubeApiClient;
    private final YoutubeCommandService youtubeCommandService;

    @Value("${app.file.json-dir}")
    private String labelingDir;


    /**
     * 특정 디렉토리의 모든 jsonl 파일을 읽어 자동으로 엔티티 생성
     */
    public void importAllFromDirectory() {
        File dir = new File(labelingDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jsonl"));
        if (files == null) return;

        for (File file : files) {
            log.info("파일 처리 시작: {}", file.getName());
            processAndSaveFromLocalFile(file.getAbsolutePath());
        }
    }


    private void processAndSaveFromLocalFile(String filePath) {

        Map<String, Map<Integer, List<String>>> allFileData = doccanoLocalParser.parseFullData(filePath);

        if (allFileData.isEmpty()) {
            return;
        }


        for (String recipeName : allFileData.keySet()) {
            Map<Integer, List<String>> labelingMap = allFileData.get(recipeName);

            // 1. 유튜브 검색
            List<SearchResult> searchResults = youtubeApiClient.searchVideos(recipeName);
            if (searchResults.isEmpty()) {
                log.warn("'{}'에 대한 유튜브 검색 결과가 없습니다. 건너뜁니다.", recipeName);
                continue;
            }

            // 2. 상세 정보 조회
            List<String> videoIds = searchResults.stream()
                .map(it -> it.getId().getVideoId()).filter(Objects::nonNull).toList();

            Map<String, Video> videoMap = youtubeApiClient.fetchVideoDetailsAsync(videoIds).join();
            Map<String, Channel> channelMap = youtubeApiClient.fetchChannelDetailsAsync(
                searchResults.stream().map(it -> it.getSnippet().getChannelId()).distinct().toList()
            ).join();

            // 3. 1차 DB 저장
            List<YoutubeVideo> savedVideos = youtubeCommandService.saveOrUpdateYoutubeData(recipeName, searchResults, videoMap, channelMap);

            // 4. 순서 기반 매칭
            Map<String, InferenceResultForVideo> inferenceResults = new HashMap<>();

            for (int i = 0; i < savedVideos.size(); i++) {
                YoutubeVideo video = savedVideos.get(i);
                int sequence = i + 1; // 파일은 1번째부터 시작

                List<String> ingredientNames = labelingMap.get(sequence);

                if (ingredientNames != null && !ingredientNames.isEmpty()) {

                    List<ExtractedIngredient> extractedIngredients = ingredientNames.stream()
                        .map(ExtractedIngredient::new)
                        .toList();

                    inferenceResults.put(video.getApiVideoId(), new InferenceResultForVideo(extractedIngredients));
                } else {
                    log.warn("매칭 실패 - [{}번째 비디오] 파일에 해당 순서의 라벨 데이터가 없습니다.", sequence);
                }
            }

            // 5. 최종 저장
            if (!inferenceResults.isEmpty()) {
                try {
                    youtubeCommandService.saveInferenceResults(savedVideos, inferenceResults, true);
                } catch (Exception e) {
                    log.error("최종 저장 중 오류 발생: {}", e.getMessage(), e);
                }
            } else {
                log.error("최종 저장 실패: 매칭된 인퍼런스 결과가 하나도 없습니다.");
            }
        }
    }

}