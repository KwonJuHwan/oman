package com.oman.domain.youtube.repository;


import com.oman.domain.youtube.entity.YoutubeChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YoutubeChannelRepository extends JpaRepository<YoutubeChannel, Long> {
    Optional<YoutubeChannel> findByApiChannelId(String apiChannelId);
}
