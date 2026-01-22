package com.oman.domain.youtube.repository;


import com.oman.domain.youtube.entity.YoutubeIngredient;
import com.oman.domain.youtube.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YoutubeIngredientRepository extends JpaRepository<YoutubeIngredient, Long> {
    List<YoutubeIngredient> findByYoutubeVideo(YoutubeVideo youtubeVideo);
}