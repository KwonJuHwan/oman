package com.oman.domain.auth.dto;

import com.oman.domain.member.entity.SocialProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private String providerId;
    private SocialProvider provider;
    private String name;
    private String email;
    private String profileImageUrl;
}