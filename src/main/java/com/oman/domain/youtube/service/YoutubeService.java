package com.oman.domain.youtube.service;

import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.oman.domain.youtube.dto.response.VideoWithCommentResponse;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.oman.domain.youtube.dto.response.VideoSearchResponse;
import com.oman.domain.youtube.dto.response.VideoCommentResponse;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final YoutubeApiService youtubeApiService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    public List<VideoWithCommentResponse> searchVideosInfo(String query) {
        List<VideoSearchResponse> initialVideoInfos = searchAndMapInitialVideoInfos(query);
        if (initialVideoInfos.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> videoIds = initialVideoInfos.stream()
            .map(VideoSearchResponse::getApiVideoId)
            .collect(Collectors.toList());

        CompletableFuture<Map<String, Video>> videoDetailsFuture = fetchVideoDetailsAsync(videoIds);

        CompletableFuture<Map<String, VideoCommentResponse>> topCommentsFuture = fetchTopCommentsAsync(videoIds);

        return combineVideoDetailsAndComments(initialVideoInfos, videoDetailsFuture.join(), topCommentsFuture.join());
    }

    // 초기 요약 정보 제공
    private List<VideoSearchResponse> searchAndMapInitialVideoInfos(String query) {
        List<SearchResult> searchResults = youtubeApiService.searchVideos(query);
        return searchResults.stream()
            .map(VideoSearchResponse::from)
            .toList();
    }

    // 각 동영상의 상세 정보 제공
    private CompletableFuture<Map<String, Video>> fetchVideoDetailsAsync(List<String> videoIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Video> videos = youtubeApiService.getVideosDetails(videoIds);
                return videos.stream()
                    .collect(Collectors.toMap(
                        Video::getId,
                        video -> video
                    ));
            } catch (Exception e) {
                return Collections.emptyMap();
            }
        }, executorService);
    }

    // 각 동영상의 첫 번째 댓글 제공
    private CompletableFuture<Map<String, VideoCommentResponse>> fetchTopCommentsAsync(List<String> videoIds) {
        List<CompletableFuture<VideoCommentResponse>> commentFutures = videoIds.stream()
            .map(videoId -> CompletableFuture.supplyAsync(() -> {
                try {
                    return youtubeApiService.getTopCommentForVideo(videoId)
                        .map(comment -> VideoCommentResponse.from(videoId, comment))
                        .orElse(null);
                } catch (Exception e) {
                    return null;
                }
            }, executorService))
            .toList();

        return CompletableFuture.allOf(commentFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> commentFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    VideoCommentResponse::getVideoId,
                    res -> res
                )));
    }

    // 최종 응답 DTO 리스트 생성
    private List<VideoWithCommentResponse> combineVideoDetailsAndComments(
        List<VideoSearchResponse> initialVideoInfos,
        Map<String, Video> fullVideoDetails,
        Map<String, VideoCommentResponse> topCommentsMap) {

        return initialVideoInfos.stream()
            .map(videoInfo -> {
                String videoId = videoInfo.getApiVideoId();
                VideoCommentResponse topComment = topCommentsMap.get(videoId);
                Video detailedVideo = fullVideoDetails.get(videoId);

                String fullDescription = videoInfo.getDescription();
                if (detailedVideo != null && detailedVideo.getSnippet() != null && detailedVideo.getSnippet().getDescription() != null) {
                    fullDescription = detailedVideo.getSnippet().getDescription();
                }

                return new VideoWithCommentResponse(
                    videoInfo.getApiVideoId(),
                    videoInfo.getTitle(),
                    fullDescription, // 전체 description 사용
                    videoInfo.getPublishedAt(),
                    videoInfo.getChannelId(),
                    videoInfo.getChannelTitle(),
                    videoInfo.getDefaultThumbnailUrl(),
                    topComment
                );
            })
            .collect(Collectors.toList());
    }

}
