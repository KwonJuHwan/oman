package com.oman.domain.youtube.repository;

import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import java.util.List;

public interface YoutubeVideoMetaRepositoryCustom {
    public List<YoutubeVideoMeta> findAllWithIngredients(List<Long> userIngredientIds);
    public List<YoutubeVideoMeta> findAllByCulinaryNameWithIngredients(String culinaryName);
}
