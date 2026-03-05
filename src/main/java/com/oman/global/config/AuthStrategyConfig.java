package com.oman.global.config;

import com.oman.domain.auth.service.strategy.SocialLoginStrategy;
import com.oman.domain.member.entity.SocialProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class AuthStrategyConfig {

    @Bean
    public Map<SocialProvider, SocialLoginStrategy> loginStrategies(List<SocialLoginStrategy> strategies) {
        return strategies.stream()
            .collect(Collectors.toMap(SocialLoginStrategy::getProvider, Function.identity()));
    }
}