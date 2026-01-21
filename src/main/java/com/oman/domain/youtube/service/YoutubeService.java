package com.oman.domain.youtube.service;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.oman.domain.culinary.entity.Culinary;
import com.oman.domain.culinary.repository.CulinaryRepository;
import com.oman.domain.youtube.dto.response.YoutubeVideoResponse;
import com.oman.domain.youtube.entity.YoutubeChannel;
import com.oman.domain.youtube.entity.YoutubeVideo;
import com.oman.domain.youtube.repository.YoutubeChannelRepository;
import com.oman.domain.youtube.repository.YoutubeVideoRepository;

import lombok.RequiredArgsConstructor;


import java.math.BigInteger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YoutubeService {

    private final YoutubeApiService youtubeApiService;
    private final YoutubeVideoRepository youtubeVideoRepository;
    private final YoutubeChannelRepository youtubeChannelRepository;
    private final CulinaryRepository culinaryRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${app.file.storage-dir}")
    private String fileStorageDir;


    @Transactional // 쓰기 트랜잭션
    public YoutubeVideoResponse processAndSaveRecipeVideos(String recipeName) {
        // 1. Culinary 엔티티 조회 또는 생성
        Culinary culinary = findOrCreateCulinary(recipeName);

        // 2. YouTube Search API 호출
        List<SearchResult> searchResults = youtubeApiService.searchVideos(recipeName);

        if (searchResults.isEmpty()) {
            return YoutubeVideoResponse.builder()
                .searchedRecipeName(recipeName)
                .videoDescriptions(new HashMap<>())
                .build();
        }

        // 3. 비디오 ID 및 채널 ID 추출
        List<String> videoIds = searchResults.stream()
            .map(item -> item.getId().getVideoId())
            .filter(Objects::nonNull)
            .toList();

        List<String> channelIds = searchResults.stream()
            .map(item -> item.getSnippet().getChannelId())
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // 4. 비디오 및 채널 상세 정보 비동기 조회
        CompletableFuture<Map<String, Video>> videoDetailsFuture = fetchVideoDetailsAsync(videoIds);
        CompletableFuture<Map<String, Channel>> channelDetailsFuture = fetchChannelDetailsAsync(channelIds);

        // 비동기 작업 결과 Join
        Map<String, Video> videoDetailsMap = videoDetailsFuture.join();
        Map<String, Channel> channelDetailsMap = channelDetailsFuture.join();

        // 5. YoutubeVideo, YoutubeChannel 엔티티 저장 및 Description 반환 Map 생성
        Map<String, String> videoDescriptionsToReturn = new HashMap<>();

        for (SearchResult searchResult : searchResults) {
            String apiVideoId = searchResult.getId().getVideoId();
            if (apiVideoId == null) continue;

            // 비디오 상세 정보
            Video videoDetail = videoDetailsMap.get(apiVideoId);
            String descriptionForResponse = getDescriptionFromVideoDetail(videoDetail);

            // 채널 정보 조회/생성 및 통계 저장
            YoutubeChannel youtubeChannel = findOrCreateChannel(searchResult.getSnippet().getChannelId(), searchResult.getSnippet().getChannelTitle(), channelDetailsMap);

            // YoutubeVideo 엔티티 저장
            saveYoutubeVideo(searchResult, videoDetail, youtubeChannel, culinary);

            videoDescriptionsToReturn.put(apiVideoId, descriptionForResponse);
        }

        return YoutubeVideoResponse.builder()
            .searchedRecipeName(recipeName)
            .videoDescriptions(videoDescriptionsToReturn)
            .build();
    }


    public Path saveDescriptionsForLabeling(String query) {
        List<SearchResult> searchResults = youtubeApiService.searchVideos(query + " 레시피");

        if (searchResults.isEmpty()) {
            System.out.println("경고: '" + query + "' 쿼리에 해당하는 비디오를 찾을 수 없어 파일을 저장하지 않습니다.");
            return null;
        }

        List<String> videoIds = searchResults.stream()
            .map(item -> item.getId().getVideoId())
            .filter(Objects::nonNull)
            .toList();

        CompletableFuture<Map<String, Video>> videoDetailsFuture = fetchVideoDetailsAsync(videoIds);
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


    /**
     * Culinary 엔티티를 검색하거나 새로 생성
     */
    private Culinary findOrCreateCulinary(String recipeName) {
        return culinaryRepository.findByName(recipeName)
            .orElseGet(() -> culinaryRepository.save(Culinary.builder().name(recipeName).build()));
    }

    /**
     * 비디오 상세 정보를 비동기로 조회
     */
    private CompletableFuture<Map<String, Video>> fetchVideoDetailsAsync(List<String> videoIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Video> details = youtubeApiService.getVideosDetails(videoIds);
                return details.stream().collect(Collectors.toMap(Video::getId, dto -> dto));
            } catch (Exception ex) {
                System.err.println("비디오 상세 정보 조회 중 오류 발생: " + ex.getMessage());
                return Collections.emptyMap();
            }
        }, executorService);
    }

    /**
     * 채널 상세 정보를 비동기로 조회
     */
    private CompletableFuture<Map<String, Channel>> fetchChannelDetailsAsync(List<String> channelIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Channel> details = youtubeApiService.getChannelsDetails(channelIds);
                return details.stream().collect(Collectors.toMap(Channel::getId, dto -> dto));
            } catch (Exception ex) {
                System.err.println("채널 상세 정보 조회 중 오류 발생: " + ex.getMessage());
                return Collections.emptyMap();
            }
        }, executorService);
    }

    private YoutubeChannel findOrCreateChannel(String apiChannelId, String channelTitle, Map<String, Channel> channelDetailsMap) {
        Optional<YoutubeChannel> existingChannelOpt = youtubeChannelRepository.findByApiChannelId(apiChannelId);
        Channel channelDetail = channelDetailsMap.get(apiChannelId);

        Long subscriberCount = Optional.ofNullable(channelDetail)
            .map(Channel::getStatistics)
            .map(com.google.api.services.youtube.model.ChannelStatistics::getSubscriberCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long viewCount = Optional.ofNullable(channelDetail)
            .map(Channel::getStatistics)
            .map(com.google.api.services.youtube.model.ChannelStatistics::getViewCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long videoCount = Optional.ofNullable(channelDetail)
            .map(Channel::getStatistics)
            .map(com.google.api.services.youtube.model.ChannelStatistics::getVideoCount)
            .map(BigInteger::longValueExact).orElse(null);

        if (existingChannelOpt.isPresent()) {
            YoutubeChannel existingChannel = existingChannelOpt.get();
            existingChannel.updateStatistics(subscriberCount, viewCount, videoCount);
            return youtubeChannelRepository.save(existingChannel);
        } else {

            YoutubeChannel newChannel = YoutubeChannel.builder()
                .apiChannelId(apiChannelId)
                .title(channelTitle)
                .subscriberCount(subscriberCount)
                .viewCount(viewCount)
                .videoCount(videoCount)
                .build();
            return youtubeChannelRepository.save(newChannel);
        }
    }

    /**
     * YoutubeVideo 엔티티를 생성하고 저장
     */
    private void saveYoutubeVideo(SearchResult searchResult, Video videoDetail, YoutubeChannel youtubeChannel, Culinary culinary) {
        String apiVideoId = searchResult.getId().getVideoId();
        String thumbnailUrl = Optional.ofNullable(videoDetail)
            .map(Video::getSnippet)
            .map(com.google.api.services.youtube.model.VideoSnippet::getThumbnails)
            .map(com.google.api.services.youtube.model.ThumbnailDetails::getMedium)
            .map(com.google.api.services.youtube.model.Thumbnail::getUrl)
            .orElse(null);
        Long viewCount = Optional.ofNullable(videoDetail)
            .map(Video::getStatistics)
            .map(com.google.api.services.youtube.model.VideoStatistics::getViewCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long likeCount = Optional.ofNullable(videoDetail)
            .map(Video::getStatistics)
            .map(com.google.api.services.youtube.model.VideoStatistics::getLikeCount)
            .map(BigInteger::longValueExact).orElse(null);
        Long commentCount = Optional.ofNullable(videoDetail)
            .map(Video::getStatistics)
            .map(com.google.api.services.youtube.model.VideoStatistics::getCommentCount)
            .map(BigInteger::longValueExact).orElse(null);

        Optional<YoutubeVideo> existingVideoOpt = youtubeVideoRepository.findByApiVideoId(apiVideoId);

        if (existingVideoOpt.isPresent()){
            YoutubeVideo existingVideo = existingVideoOpt.get();;
            existingVideo.updateStatistics(viewCount,likeCount,commentCount);
        }
        else {
            YoutubeVideo newVideo = YoutubeVideo.builder()
                .apiVideoId(apiVideoId)
                .title(searchResult.getSnippet().getTitle())
                .thumbnailUrl(thumbnailUrl)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .channel(youtubeChannel)
                .culinary(culinary)
                .build();
            youtubeVideoRepository.save(newVideo);
        }

    }

    /**
     * Video 상세 정보에서 description을 추출
     */
    private String getDescriptionFromVideoDetail(Video videoDetail) {
        return Optional.ofNullable(videoDetail)
            .map(Video::getSnippet)
            .map(com.google.api.services.youtube.model.VideoSnippet::getDescription)
            .orElse("");
    }
}
