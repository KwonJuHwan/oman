package com.oman.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oman.domain.member.dto.SearchHistoryRequestDto;
import com.oman.domain.member.entity.SearchType;
import com.oman.domain.recipe.dto.response.IngredientSimpleDto;
import com.oman.global.error.ErrorCode;
import com.oman.global.error.exception.RedisException;
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
    private static final String REDIS_KEY_PREFIX = "search_history:";

    private String generateKey(String email, SearchType type) {
        return REDIS_KEY_PREFIX + email + ":" + type.name();
    }

    public void saveRecentSearch(String email, SearchHistoryRequestDto request) {
        String key = generateKey(email, request.getType());
        long currentTime = System.currentTimeMillis();

        try {
            String jsonValue = objectMapper.writeValueAsString(request);
            stringRedisTemplate.opsForZSet().add(key, jsonValue, currentTime);
            stringRedisTemplate.opsForZSet().removeRange(key, 0, -(MAX_HISTORY_SIZE + 1));
        } catch (JsonProcessingException e) {
            throw new RedisException(ErrorCode.REDIS_SERIALIZATION_FAILED, e);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILED, e);
        }
    }
    public List<IngredientSimpleDto> getRecentSearches(String email, SearchType type) {
        String key = generateKey(email, type);
        return fetchRecentSearches(key);
    }

    private List<IngredientSimpleDto> fetchRecentSearches(String key) {
        Set<String> recentSearches = stringRedisTemplate.opsForZSet().reverseRange(key, 0, MAX_HISTORY_SIZE - 1);
        if (recentSearches == null || recentSearches.isEmpty()) {
            return List.of();
        }
        return recentSearches.stream()
            .map(this::parseToDto)
            .filter(Objects::nonNull)
            .toList();
    }

    private IngredientSimpleDto parseToDto(String json) {
        try {
            SearchHistoryRequestDto dto = objectMapper.readValue(json, SearchHistoryRequestDto.class);
            return new IngredientSimpleDto(dto.getKeywordId(), dto.getKeyword());
        } catch (JsonProcessingException e) {
            // 조회 중 일부 파싱 실패 시 전체를 터뜨리지 않고 해당 항목만 무시
            return null;
        }
    }

    public void deleteRecentSearch(String email, SearchHistoryRequestDto request) {
        String key = generateKey(email, request.getType());
        try {
            String jsonValue = objectMapper.writeValueAsString(request);
            stringRedisTemplate.opsForZSet().remove(key, jsonValue);
        } catch (JsonProcessingException e) {
            throw new RedisException(ErrorCode.REDIS_SERIALIZATION_FAILED, e);
        } catch (Exception e) {
            throw new RedisException(ErrorCode.REDIS_OPERATION_FAILED, e);
        }
    }
}