package com.oman.domain.youtube.repository;

import com.oman.domain.youtube.entity.YoutubeVideoMeta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YoutubeVideoMetaRepository extends JpaRepository<YoutubeVideoMeta,Long>,YoutubeVideoMetaRepositoryCustom {
    List<YoutubeVideoMeta> findAllByYoutubeVideoIdIn(List<Long> videoIds);

    void deleteByYoutubeVideoIdIn(List<Long> videoIds);
}
