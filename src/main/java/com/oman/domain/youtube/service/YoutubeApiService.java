package com.oman.domain.youtube.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class YoutubeApiService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final long NUMBER_OF_VIDEOS_RETURNED = 10;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance(); // <-- 이 줄로 변경


    private YouTube youtube;

    public YoutubeApiService() {
        youtube = new YouTube.Builder(new NetHttpTransport(), JSON_FACTORY, request -> {
            // 필요하다면 request 초기화 로직 추가 (예: 타임아웃, 헤더 설정)
        }).setApplicationName("youtube-video-search").build(); // 애플리케이션 이름은 프로젝트에 맞게 변경
    }

    /**
     * 키워드로 유튜브 영상을 검색하고 상위 N개의 검색 결과를 반환합니다.
     * @param query 검색 키워드
     * @return 검색 결과 영상 목록 (SearchResult 객체)
     */
    public List<SearchResult> searchVideos(String query) {
        try {
            YouTube.Search.List search = youtube.search().list(Collections.singletonList("id,snippet"));

            search.setKey(apiKey);
            search.setQ(query);
            search.setType(Collections.singletonList("video"));
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            search.setSafeSearch("moderate");

            SearchListResponse searchResponse = search.execute();
            return searchResponse.getItems(); // SearchResult 객체 리스트 반환

        } catch (GoogleJsonResponseException e) {
            System.err.println("API 호출 오류 (GoogleJsonResponseException): " + e.getDetails());
            // 실제 환경에서는 Custom Exception으로 래핑하여 던지거나, 적절한 오류 응답 처리
            throw new RuntimeException("YouTube API 호출 중 오류 발생: " + e.getMessage(), e);
        } catch (IOException e) {
            System.err.println("IO 오류 (IOException): " + e.getMessage());
            throw new RuntimeException("네트워크 또는 데이터 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
