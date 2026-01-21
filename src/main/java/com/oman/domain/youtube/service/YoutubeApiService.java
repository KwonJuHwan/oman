package com.oman.domain.youtube.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.oman.global.error.exception.YoutubeApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.oman.domain.youtube.exception.*;



@Service
public class YoutubeApiService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final long NUMBER_OF_VIDEOS_RETURNED = 20;
    private static final String VIDEO_DURATION_FILTER = "medium";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    private YouTube youtube;

    public YoutubeApiService() {
        youtube = new YouTube.Builder(new NetHttpTransport(), JSON_FACTORY, request -> {
        }).setApplicationName("youtube-video-search").build();
    }

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
