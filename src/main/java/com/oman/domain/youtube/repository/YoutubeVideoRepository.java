package com.oman.domain.youtube.repository;

import com.oman.domain.youtube.entity.YoutubeVideo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, Long> {

    List<YoutubeVideo> findAllByApiVideoIdIn(List<String> videoIds);

    @Query("SELECT v FROM YoutubeVideo v " +
        "JOIN FETCH v.culinary c " +
        "JOIN FETCH v.videoIngredients vi " +
        "JOIN FETCH vi.ingredient i " + // 마스터 재료까지 가져오기
        "WHERE c.name = :name")
    List<YoutubeVideo> findAllByCulinaryName(@Param("name") String name);
}
