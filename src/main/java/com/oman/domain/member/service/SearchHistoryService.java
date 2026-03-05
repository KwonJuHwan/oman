package com.oman.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oman.domain.member.dto.SearchHistoryRequestDto;
import com.oman.domain.recipe.dto.response.IngredientSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final int MAX_HISTORY_SIZE = 10;

    public void saveRecentSearch(String email, SearchHistoryRequestDto request) {
        String key = "search_history:" + email + ":" + request.getType().name();
        long currentTime = System.currentTimeMillis();

        try {
            String jsonValue = objectMapper.writeValueAsString(request);
            stringRedisTemplate.opsForZSet().add(key, jsonValue, currentTime);
            stringRedisTemplate.opsForZSet().removeRange(key, 0, -(MAX_HISTORY_SIZE + 1));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis JSON 직렬화 실패", e);
        }
    }

    public List<IngredientSimpleDto> getRecentRecipeSearches(String email) {
        String key = "search_history:" + email + ":RECIPE";
        Set<String> recentSearches = stringRedisTemplate.opsForZSet().reverseRange(key, 0, MAX_HISTORY_SIZE - 1);

        if (recentSearches == null || recentSearches.isEmpty()) {
            return List.of();
        }
        return recentSearches.stream()
            .map(json -> {
                try {
                    SearchHistoryRequestDto dto = objectMapper.readValue(json, SearchHistoryRequestDto.class);
                    return new IngredientSimpleDto(dto.getKeywordId(), dto.getKeyword());
                } catch (JsonProcessingException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public List<IngredientSimpleDto> getRecentIngredientSearches(String email) {
        String key = "search_history:" + email + ":INGREDIENT";
        Set<String> recentSearches = stringRedisTemplate.opsForZSet().reverseRange(key, 0, MAX_HISTORY_SIZE - 1);

        if (recentSearches == null || recentSearches.isEmpty()) {
            return List.of();
        }

        return recentSearches.stream()
            .map(json -> {
                try {
                    SearchHistoryRequestDto dto = objectMapper.readValue(json, SearchHistoryRequestDto.class);
                    return new IngredientSimpleDto(dto.getKeywordId(), dto.getKeyword());
                } catch (JsonProcessingException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public void deleteRecentSearch(String email, SearchHistoryRequestDto request) {
        String key = "search_history:" + email + ":" + request.getType().name();
        try {
            String jsonValue = objectMapper.writeValueAsString(request);
            stringRedisTemplate.opsForZSet().remove(key, jsonValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 검색 기록 삭제 실패", e);
        }
    }
}