package com.oman.domain.youtube.service;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.oman.domain.culinary.repository.CulinaryRepository;
import com.oman.domain.fastapi.client.FastApiClient;
import com.oman.domain.fastapi.dto.FastApiInferenceResponse;
import com.oman.domain.youtube.dto.response.YoutubeVideoResponse;
import com.oman.domain.youtube.entity.YoutubeVideo;
import com.oman.domain.youtube.repository.YoutubeChannelRepository;
import com.oman.domain.youtube.repository.YoutubeVideoRepository;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YoutubeProcessService {

    private final YoutubeApiClient youtubeApiClient;
    private final YoutubeCommandService youtubeCommandService;
    private final FastApiClient fastApiClient;

    @Value("${app.file.storage-dir}")
    private String fileStorageDir;

    /**
     * 전체 프로세스 메인 메서드
     */
    public String processAndSaveRecipeVideos(String recipeName) {
        // YouTube Search API 호출
        List<SearchResult> searchResults = youtubeApiClient.searchVideos(recipeName);
        if (searchResults.isEmpty()) {
            log.warn("No YouTube videos found for recipe: {}", recipeName);
            return "fail";
        }

        // 비디오/채널 ID 추출 및 상세 정보 비동기 조회
        List<String> videoIds =
            searchResults.stream().map(
                it -> it.getId().getVideoId()).filter(Objects::nonNull).toList();
        List<String> channelIds =
            searchResults.stream().map(
                it -> it.getSnippet().getChannelId()).distinct().toList();

        CompletableFuture<Map<String, Video>> videoFuture =
            youtubeApiClient.fetchVideoDetailsAsync(videoIds);
        CompletableFuture<Map<String, Channel>> channelFuture =
            youtubeApiClient.fetchChannelDetailsAsync(channelIds);

        Map<String, Video> videoMap = videoFuture.join();
        Map<String, Channel> channelMap = channelFuture.join();

        // 3. 1차 DB 저장 및 업데이트
        List<YoutubeVideo> savedVideos = youtubeCommandService.saveOrUpdateYoutubeData(recipeName, searchResults, videoMap, channelMap);

        // 4. FastAPI 모델 추론 호출
        Map<String, String> videoDescriptions = savedVideos.stream()
            .collect(Collectors.toMap(
                YoutubeVideo::getApiVideoId,
                v -> getDescriptionFromVideoDetail(videoMap.get(v.getApiVideoId()))
            ));

        YoutubeVideoResponse fastApiRequestDto = YoutubeVideoResponse.builder()
            .searchedRecipeName(recipeName)
            .videoDescriptions(videoDescriptions)
            .build();

        log.info("Calling FastAPI for inference: {}", recipeName);
        FastApiInferenceResponse fastApiResult = fastApiClient.callInferenceApi(fastApiRequestDto);

        // 5. 추론 결과 최종 저장
        if (fastApiResult != null && fastApiResult.getResults() != null) {
            youtubeCommandService.saveInferenceResults(savedVideos, fastApiResult.getResults());
        }

        return "success!~";
    }

    private String getDescriptionFromVideoDetail(Video videoDetail) {
        return Optional.ofNullable(videoDetail)
            .map(Video::getSnippet)
            .map(VideoSnippet::getDescription)
            .orElse("");
    }

    // DATA labeling용 데이터 뽑는 용도 - Training Model
    public Path saveDescriptionsForLabeling(String query) {
        List<SearchResult> searchResults = youtubeApiClient.searchVideos(query + " 레시피");

        if (searchResults.isEmpty()) {
            System.out.println("경고: '" + query + "' 쿼리에 해당하는 비디오를 찾을 수 없어 파일을 저장하지 않습니다.");
            return null;
        }

        List<String> videoIds = searchResults.stream()
            .map(item -> item.getId().getVideoId())
            .filter(Objects::nonNull)
            .toList();

        CompletableFuture<Map<String, Video>> videoDetailsFuture = youtubeApiClient.fetchVideoDetailsAsync(videoIds);
        Map<String, Video> videoDetailsMap = videoDetailsFuture.join();

        Map<String, String> videoDescriptionsForFile = new HashMap<>();
        for (SearchResult searchResult : searchResults) {
            String apiVideoId = searchResult.getId().getVideoId();
            if (apiVideoId == null) continue;

            Video videoDetail = videoDetailsMap.get(apiVideoId);
            String description = getDescriptionFromVideoDetail(videoDetail);

            videoDescriptionsForFile.put(apiVideoId, description);
        }

        if (videoDescriptionsForFile.isEmpty()) {
            System.out.println("경고: 추출된 description이 없어 파일을 저장하지 않습니다.");
            return null;
        }

        StringBuilder textContentBuilder = new StringBuilder();

        textContentBuilder.append("[검색 쿼리]: ").append(query).append("\n\n");
        textContentBuilder.append("--- 동영상 상세 설명 목록 ---\n");
        textContentBuilder.append("--------------------------------------------------------------------------------\n");

        int videoCount = 0;
        for (Map.Entry<String, String> entry : videoDescriptionsForFile.entrySet()) {
            videoCount++;
            textContentBuilder.append("<<")
                .append(videoCount)
                .append("번째 동영상 설명 (ID: ")
                .append(entry.getKey())
                .append(")>>\n");
            textContentBuilder.append(entry.getValue()).append("\n\n");
            textContentBuilder.append("--------------------------------------------------------------------------------\n");
        }

        // 6. 파일 저장
        try {
            Path storagePath = Paths.get(fileStorageDir);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String sanitizedQuery = query.replaceAll("[^a-zA-Z0-9가-힣]", "_");
            if (sanitizedQuery.isEmpty()) {
                sanitizedQuery = "unnamed_query";
            }
            String fileName = String.format("%s_%s.txt", sanitizedQuery, timestamp);
            Path filePath = storagePath.resolve(fileName);

            Files.writeString(filePath, textContentBuilder.toString());
            System.out.println("동영상 description .txt 파일 저장 완료: " + filePath.toAbsolutePath());
            return filePath;

        } catch (IOException e) {
            System.err.println("동영상 description .txt 파일 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
