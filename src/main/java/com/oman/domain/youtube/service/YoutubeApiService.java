package com.oman.domain.youtube.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.oman.global.error.exception.YoutubeApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.oman.domain.youtube.exception.*;



@Service
public class YoutubeApiService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final long NUMBER_OF_VIDEOS_RETURNED = 10;
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

    public Optional<Comment> getTopCommentForVideo(String videoId) {
        try {
            YouTube.CommentThreads.List commentThreads = youtube.commentThreads().list(Collections.singletonList("snippet"));

            commentThreads.setKey(apiKey);
            commentThreads.setVideoId(videoId);
            commentThreads.setMaxResults(1L);
            commentThreads.setOrder("relevance");
            commentThreads.setTextFormat("plainText");

            CommentThreadListResponse response = commentThreads.execute();

            if (response != null && !response.getItems().isEmpty()) {
                return Optional.ofNullable(response.getItems().get(0).getSnippet().getTopLevelComment());
            } else {
                return Optional.empty();
            }

        }catch (GoogleJsonResponseException e) {
            // 동영상 ID가 잘못되었거나, 삭제되었거나, 댓글 기능이 아예 비활성화된 경우
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            }
            throw createYoutubeApiException(e);
        } catch (IOException e) {
            throw new YoutubeNetworkException(e);
        } catch (Exception e) {
            throw new YoutubeApiGeneralException(e);
        }
    }

    public List<Video> getVideosDetails(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            YouTube.Videos.List videosList = youtube.videos().list(Collections.singletonList("snippet,contentDetails"));
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
