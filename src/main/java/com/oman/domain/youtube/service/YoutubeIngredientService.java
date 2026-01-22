package com.oman.domain.youtube.service;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.culinary.repository.IngredientRepository;
import com.oman.domain.youtube.repository.YoutubeIngredientRepository;
import com.oman.domain.fastapi.dto.ExtractedIngredient;
import com.oman.domain.youtube.entity.YoutubeIngredient;
import com.oman.domain.youtube.entity.YoutubeVideo;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeIngredientService {

    private final IngredientRepository ingredientRepository;
    private final YoutubeIngredientRepository youtubeIngredientRepository;

    // 필터링할 조사 및 기타 불용어
    private static final String[] EXCLUDE_PARTICLES = {"을", "를", "과", "는", "으로"};
    // 숫자 제거 패턴 (숫자, 숫자+단위) - ex: 100g, 2개, 3컵
    private static final Pattern NUMBER_AND_UNIT_PATTERN = Pattern.compile("(\\d+)([mgkL컵쪽])+?");
    private static final Pattern PURE_NUMBER_PATTERN = Pattern.compile("\\d+");
    // 특수문자 및 공백 제거 패턴 (한글, 영어, 숫자 제외)
    private static final Pattern SPECIAL_CHARS_AND_SPACES_PATTERN = Pattern.compile("[^가-힣a-zA-Z0-9]");



    @Transactional
    public List<YoutubeIngredient> processAndSaveYoutubeIngredients(YoutubeVideo youtubeVideo, List<ExtractedIngredient> extractedIngredients) {
        if (extractedIngredients == null || extractedIngredients.isEmpty()) {
            return Collections.emptyList();
        }

        List<YoutubeIngredient> savedYoutubeIngredients = extractedIngredients.stream()
            .map(fastApiExtractedIngredient -> {
                String extractedName = fastApiExtractedIngredient.getIngredient();
                String filteredName = filterIngredientName(extractedName);

                if (filteredName.isEmpty()) {
                    log.debug("필터링 후 빈 문자열이 된 재료: '{}'. 저장하지 않습니다.", extractedName);
                    return null;
                }

                // Ingredient 엔티티 조회 또는 생성
                Ingredient ingredient = findOrCreateIngredient(filteredName);

                return YoutubeIngredient.builder()
                    .name(filteredName)
                    .extractedName(extractedName)
                    .ingredient(ingredient)
                    .youtubeVideo(youtubeVideo)
                    .build();

            })
            .filter(Objects::nonNull)
            .toList();

        return youtubeIngredientRepository.saveAll(savedYoutubeIngredients);
    }

    private String filterIngredientName(String rawName) {
        String filtered = rawName;

        for (String particle : EXCLUDE_PARTICLES) {
            filtered = filtered.replaceAll(Pattern.quote(particle) + "$", "");
        }

        filtered = NUMBER_AND_UNIT_PATTERN.matcher(filtered).replaceAll("");
        filtered = PURE_NUMBER_PATTERN.matcher(filtered).replaceAll("");

        filtered = SPECIAL_CHARS_AND_SPACES_PATTERN.matcher(filtered).replaceAll("");
        filtered = filtered.trim();
        return filtered;
    }

    public Ingredient findOrCreateIngredient(String name) {
        return ingredientRepository.findByName(name)
            .orElseGet(() -> {
                log.info("새로운 재료 엔티티 생성: {}", name);
                return ingredientRepository.save(Ingredient.builder().name(name).build());
            });
    }
}