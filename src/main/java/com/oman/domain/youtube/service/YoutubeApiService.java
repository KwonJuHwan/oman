package com.oman.domain.youtube.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.oman.global.error.exception.YoutubeApiException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.oman.domain.youtube.exception.*;



@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeApiService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final long NUMBER_OF_VIDEOS_RETURNED = 20;
    private static final String VIDEO_DURATION_FILTER = "medium";


    private final YouTube youtube;
    private final ExecutorService youtubeApiExecutorService;

    public List<SearchResult> searchVideos(String query) {
        try {

            YouTube.Search.List search = youtube.search().list(Collections.singletonList("id,snippet"));

            search.setKey(apiKey);
            search.setQ(query);
            search.setType(Collections.singletonList("video"));
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            search.setSafeSearch("moderate");
            search.setVideoDuration(VIDEO_DURATION_FILTER);

            SearchListResponse searchResponse = search.execute();
            if (searchResponse == null || searchResponse.getItems().isEmpty()) {
                return Collections.emptyList();
            }
            return searchResponse.getItems();
        } catch (GoogleJsonResponseException e) {
            throw createYoutubeApiException(e);
        } catch (IOException e) {
            throw new YoutubeNetworkException(e);
        } catch (Exception e) { // 그 외 모든 예상치 못한 예외
            throw new YoutubeApiGeneralException(e);
        }
    }

    public List<Video> getVideosDetails(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            YouTube.Videos.List videosList = youtube.videos().list(Collections.singletonList("snippet,contentDetails,statistics"));
            videosList.setKey(apiKey);
            videosList.setId(videoIds);
            videosList.setMaxResults((long) videoIds.size());

            VideoListResponse videoListResponse = videosList.execute();

            if (videoListResponse == null || videoListResponse.getItems().isEmpty()) {
                return Collections.emptyList();
            }
            return videoListResponse.getItems();

        } catch (GoogleJsonResponseException e) {
            throw createYoutubeApiException(e);
        } catch (IOException e) {
            throw new YoutubeNetworkException(e);
        } catch (Exception e) {
            throw new YoutubeApiGeneralException(e);
        }
    }

    public List<Channel> getChannelsDetails(List<String> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            YouTube.Channels.List channelsList = youtube.channels().list(Collections.singletonList("snippet,statistics"));
            channelsList.setKey(apiKey);
            channelsList.setId(channelIds);
            channelsList.setMaxResults((long) channelIds.size());

            ChannelListResponse channelListResponse = channelsList.execute();
            if (channelListResponse == null || channelListResponse.getItems().isEmpty()) {
                return Collections.emptyList();
            }
            return channelListResponse.getItems();
        } catch (GoogleJsonResponseException e) {
            throw createYoutubeApiException(e);
        } catch (IOException e) {
            throw new YoutubeNetworkException(e);
        } catch (Exception e) {
            throw new YoutubeApiGeneralException(e);
        }
    }

    public CompletableFuture<Map<String, Video>> fetchVideoDetailsAsync(List<String> videoIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Video> details = getVideosDetails(videoIds);
                return details.stream().collect(Collectors.toMap(Video::getId, dto -> dto));
            } catch (Exception ex) {
                log.error("비디오 상세 정보 비동기 조회 중 오류 발생: {}", ex.getMessage(), ex);
                return Collections.emptyMap();
            }
        }, youtubeApiExecutorService);
    }


    public CompletableFuture<Map<String, Channel>> fetchChannelDetailsAsync(List<String> channelIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Channel> details = getChannelsDetails(channelIds);
                return details.stream().collect(Collectors.toMap(Channel::getId, dto -> dto));
            } catch (Exception ex) {
                log.error("채널 상세 정보 비동기 조회 중 오류 발생: {}", ex.getMessage(), ex);
                return Collections.emptyMap();
            }
        }, youtubeApiExecutorService);
    }

    private YoutubeApiException createYoutubeApiException(GoogleJsonResponseException e) {
        int statusCode = e.getStatusCode();
        String apiResponseDetail = e.getDetails().getMessage();


        switch (statusCode) {
            case 400:
                return new YoutubeInvalidParameterException(e);
            case 401:
                return new YoutubeInvalidApiKeyException(e);
            case 403:
                if (apiResponseDetail != null && apiResponseDetail.contains("quotaExceeded")) {
                    return new YoutubeQuotaExceededException(e);
                } else {
                    return new YoutubeAccessDeniedException(e);
                }
            case 404:
                return new YoutubeVideoNotFoundException(e);
            default:
                return new YoutubeApiGeneralException(e);
        }
    }
}
