package com.oman.domain.youtube.repository;


import com.oman.domain.statistic.dto.IngredientCountDto;
import java.util.List;


public interface YoutubeIngredientRepositoryCustom {

    // 특정 요리에 속한 비디오들에서 사용된 재료와 그 개수를 집계
    List<IngredientCountDto> countIngredientsByCulinaryId(Long culinaryId);

    // 특정 요리에 연결된 전체 비디오 개수 카운트
    long countTotalVideosByCulinaryId(Long culinaryId);
}
