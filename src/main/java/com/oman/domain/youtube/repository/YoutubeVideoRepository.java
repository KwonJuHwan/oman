package com.oman.domain.youtube.repository;

import com.oman.domain.youtube.entity.YoutubeVideo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, Long> {

    List<YoutubeVideo> findAllByApiVideoIdIn(List<String> videoIds);
}
