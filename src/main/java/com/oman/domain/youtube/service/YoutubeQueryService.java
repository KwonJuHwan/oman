package com.oman.domain.youtube.service;

import com.oman.domain.statistic.dto.IngredientCountDto;
import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import com.oman.domain.youtube.repository.YoutubeIngredientRepository;
import com.oman.domain.youtube.repository.YoutubeVideoMetaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YoutubeQueryService {
    private final YoutubeIngredientRepository youtubeIngredientRepository;
    private final YoutubeVideoMetaRepository youtubeVideoMetaRepository;

    public List<IngredientCountDto> getIngredientCountsForCulinary(Long culinaryId) {
        return youtubeIngredientRepository.countIngredientsByCulinaryId(culinaryId);
    }

    public long getTotalVideoCount(Long culinaryId) {
        return youtubeIngredientRepository.countTotalVideosByCulinaryId(culinaryId);
    }
    public List<YoutubeVideoMeta> findAllWithIngredients(List<Long> userIngredientIds){
        return youtubeVideoMetaRepository.findAllWithIngredients(userIngredientIds);
    }
    public List<YoutubeVideoMeta> findAllByCulinaryNameWithIngredients(String culinaryName){
        return youtubeVideoMetaRepository.findAllByCulinaryNameWithIngredients(culinaryName);
    }

}
