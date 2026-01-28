package com.oman.domain.fastapi.client;

import com.oman.domain.fastapi.dto.FastApiInferenceResponse;
import com.oman.domain.youtube.dto.response.YoutubeVideoResponse; // 기존 DTO 임포트
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class FastApiClient {

    private final WebClient webClient;

    public FastApiClient(
        WebClient.Builder webClientBuilder,
        @Value("${fastapi.inference.url}") String fastapiInferenceUrl,
        @Value("${fastapi.inference.api.key}") String fastapiInferenceApiKey) {

        this.webClient = webClientBuilder.baseUrl(fastapiInferenceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-API-Key", fastapiInferenceApiKey) // FastAPI API Key 헤더 설정
            .build();
        log.info("FastApiClient initialized with base URL: {}", fastapiInferenceUrl);
    }


    public Mono<FastApiInferenceResponse> callInferenceApi(YoutubeVideoResponse youtubeVideoResponse) {

        return webClient.post()
            .bodyValue(youtubeVideoResponse)
            .retrieve()
            .bodyToMono(FastApiInferenceResponse.class)
            .timeout(Duration.ofSeconds(60))
            .doOnError(e -> log.error("FastAPI Error: {}", e.getMessage()));
    }
}