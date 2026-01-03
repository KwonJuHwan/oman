package com.oman.domain.youtube.service;

import com.oman.domain.youtube.dto.response.YoutubeVideoSearchResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final YoutubeApiService youtubeApiService;

    /**
     * 검색어에 해당하는 유튜브 동영상 정보를 조회하여 응답 DTO 리스트로 반환합니다.
     * @param query 검색어
     * @return 상위 10개 동영상의 정보 (YoutubeVideoSearchResponse 리스트)
     */
    public List<YoutubeVideoSearchResponse> searchVideosAndRespond(String query) {
        // YoutubeApiService를 통해 원본 SearchResult 리스트를 가져옴
        List<SearchResult> searchResults = youtubeApiService.searchVideos(query);

        // 가져온 SearchResult 리스트를 YoutubeVideoSearchResponse DTO 리스트로 변환
        return searchResults.stream()
            .map(YoutubeVideoSearchResponse::from) // SearchResult -> YoutubeVideoSearchResponse 변환
            .collect(Collectors.toList());
    }
}
