package com.oman.domain.culinary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oman.domain.culinary.repository.IngredientRepository;
import com.oman.domain.recipe.dto.response.IngredientSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientAutocompleteService {

    private final IngredientRepository ingredientRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String INGREDIENT_CACHE_KEY = "cache:ingredients:all";

    public List<IngredientSimpleDto> getAutocompleteResults(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();

        List<IngredientSimpleDto> allIngredients = getAllIngredientsFromRedisOrDb();

        return allIngredients.stream()
            .filter(dto -> dto.name().contains(keyword))
            .limit(10)
            .toList();
    }

    private List<IngredientSimpleDto> getAllIngredientsFromRedisOrDb() {
        String cachedJson = redisTemplate.opsForValue().get(INGREDIENT_CACHE_KEY);

        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                return fetchAndCacheIngredients();
            }
        }

        return fetchAndCacheIngredients();
    }

    private List<IngredientSimpleDto> fetchAndCacheIngredients() {
        List<IngredientSimpleDto> ingredientsFromDb = ingredientRepository.findAll().stream()
            .map(i -> new IngredientSimpleDto(i.getId(), i.getName()))
            .toList();

        try {
            String jsonToCache = objectMapper.writeValueAsString(ingredientsFromDb);
            redisTemplate.opsForValue().set(INGREDIENT_CACHE_KEY, jsonToCache, Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            // 캐싱 실패해도 서비스는 동작해야 하므로 예외를 던지지 않고 넘어감
        }

        return ingredientsFromDb;
    }
}