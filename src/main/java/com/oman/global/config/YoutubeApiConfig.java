package com.oman.global.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Configuration
public class YoutubeApiConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public YouTube youtube() throws GeneralSecurityException, IOException {
        final YouTube youtube = new YouTube.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            request -> {}
        ).setApplicationName("youtube-video-search")
            .build();
        log.info("YouTube Data API client initialized.");
        return youtube;
    }
}
