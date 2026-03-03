package com.oman.domain.auth.service.strategy;

import com.oman.domain.auth.dto.OAuth2UserInfo;
import com.oman.domain.member.entity.SocialProvider;

public interface SocialLoginStrategy {
    SocialProvider getProvider();

    OAuth2UserInfo verifyTokenAndGetUserInfo(String token);
}