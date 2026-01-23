package com.oman.domain.youtube.service;

import com.oman.domain.culinary.entity.Ingredient;
import com.oman.domain.fastapi.dto.ExtractedIngredient;
import com.oman.domain.youtube.entity.YoutubeIngredient;
import com.oman.domain.youtube.entity.YoutubeVideo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IngredientProcessor {
    private static final String[] EXCLUDE_PARTICLES = {"을", "를", "과", "는", "으로"};
    private static final Pattern NUMBER_AND_UNIT_PATTERN = Pattern.compile("(\\d+)([mgkL컵쪽])+?");
    private static final Pattern PURE_NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern SPECIAL_CHARS_AND_SPACES_PATTERN = Pattern.compile("[^가-힣a-zA-Z0-9]");

    /**
     * 필터링 로직만 수행하여 DTO나 엔티티 목록으로 변환
     */
    public List<YoutubeIngredient> filterAndCreateEntities(YoutubeVideo video, List<ExtractedIngredient> extractedIngredients, Map<String, Ingredient> ingredientMasterMap) {
        if (extractedIngredients == null) return Collections.emptyList();

        return extractedIngredients.stream()
            .map(ext -> {
                String filteredName = filterIngredientName(ext.getIngredient());
                if (filteredName.isEmpty()) return null;

                // 마스터 재료(Ingredient) 매핑은 호출부에서 관리하거나 Map으로 전달받음
                Ingredient master = ingredientMasterMap.get(filteredName);

                return YoutubeIngredient.builder()
                    .name(filteredName)
                    .extractedName(ext.getIngredient())
                    .ingredient(master)
                    .youtubeVideo(video)
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public String filterIngredientName(String rawName) {
        String filtered = rawName;
        for (String particle : EXCLUDE_PARTICLES) {
            filtered = filtered.replaceAll(Pattern.quote(particle) + "$", "");
        }
        filtered = NUMBER_AND_UNIT_PATTERN.matcher(filtered).replaceAll("");
        filtered = PURE_NUMBER_PATTERN.matcher(filtered).replaceAll("");
        filtered = SPECIAL_CHARS_AND_SPACES_PATTERN.matcher(filtered).replaceAll("");
        return filtered.trim();
    }
}
