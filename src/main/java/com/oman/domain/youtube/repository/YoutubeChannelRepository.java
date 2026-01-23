package com.oman.domain.youtube.repository;


import com.oman.domain.youtube.entity.YoutubeChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YoutubeChannelRepository extends JpaRepository<YoutubeChannel, Long> {

    List<YoutubeChannel> findAllByApiChannelIdIn(List<String> channelIds);
}
