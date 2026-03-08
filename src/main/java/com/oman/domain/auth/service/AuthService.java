package com.oman.domain.auth.service;

import com.oman.domain.auth.dto.AuthResponse;
import com.oman.domain.auth.dto.OAuth2UserInfo;
import com.oman.domain.auth.entity.RefreshToken;
import com.oman.domain.auth.repository.RefreshTokenRepository;
import com.oman.domain.auth.security.JwtTokenProvider;
import com.oman.domain.auth.service.strategy.SocialLoginStrategy;
import com.oman.domain.member.entity.Member;
import com.oman.domain.member.entity.SocialProvider;
import com.oman.domain.member.repository.MemberRepository;
import com.oman.global.error.ErrorCode;
import com.oman.global.error.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {


    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<SocialProvider, SocialLoginStrategy> loginStrategies;

    @Transactional
    public AuthResponse login(SocialProvider provider, String idToken) {
        SocialLoginStrategy strategy = loginStrategies.get(provider);
        OAuth2UserInfo userInfo = strategy.verifyTokenAndGetUserInfo(idToken);

        Member member = memberRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
            .orElseGet(() -> registerNewMember(userInfo));

        log.info("로그인 성공 - 이메일: [{}], 제공자: [{}]",
            member.getEmail(), provider.name());

        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        RefreshToken savedRefreshToken = refreshTokenRepository.findById(member.getEmail())
            .map(token -> {
                token.updateToken(refreshToken);
                return token;
            })
            .orElseGet(() -> RefreshToken.builder()
                .memberEmail(member.getEmail())
                .token(refreshToken)
                .build());
        refreshTokenRepository.save(savedRefreshToken);

        log.debug("토큰 발급 완료 - 이메일: [{}]", member.getEmail());
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .memberId(member.getId())
            .build();
    }

    @Transactional
    public AuthResponse reissueToken(String oldRefreshToken) {
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(oldRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findById(email)
            .orElseThrow(() -> new AuthException(ErrorCode.LOGGED_OUT_USER));

        // RTR 보안
        if (!storedToken.getToken().equals(oldRefreshToken)) {
            refreshTokenRepository.delete(storedToken);
            throw new AuthException(ErrorCode.STOLEN_TOKEN_DETECTED);
        }

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException(ErrorCode.STOLEN_TOKEN_DETECTED));

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        storedToken.updateToken(newRefreshToken);
        log.info("토큰 갱신 완료 - 이메일: [{}]", email);
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .memberId(member.getId())
            .build();
    }

    private Member registerNewMember(OAuth2UserInfo userInfo) {
        log.info("신규 사용자 가입 진행 - 이메일: [{}]", userInfo.getEmail());
        Member newMember = Member.builder()
            .email(userInfo.getEmail())
            .name(userInfo.getName())
            .provider(userInfo.getProvider())
            .providerId(userInfo.getProviderId())
            .profileImageUrl(userInfo.getProfileImageUrl())
            .build();
        return memberRepository.save(newMember);
    }
}