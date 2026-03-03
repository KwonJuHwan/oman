package com.oman.domain.auth.service.strategy;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.oman.domain.auth.dto.OAuth2UserInfo;
import com.oman.domain.member.entity.SocialProvider;
import com.oman.global.error.ErrorCode;
import com.oman.global.error.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class GoogleLoginStrategy implements SocialLoginStrategy {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public OAuth2UserInfo verifyTokenAndGetUserInfo(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                return OAuth2UserInfo.builder()
                    .providerId(payload.getSubject())
                    .provider(SocialProvider.GOOGLE)
                    .name((String) payload.get("name"))
                    .email(payload.getEmail())
                    .profileImageUrl((String) payload.get("picture"))
                    .build();
            } else {
                throw new AuthException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }
        } catch (Exception e) {
            throw new RuntimeException("구글 로그인 토큰 검증 중 오류 발생", e);
        }
    }
}