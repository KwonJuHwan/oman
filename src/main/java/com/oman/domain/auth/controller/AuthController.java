package com.oman.domain.auth.controller;

import com.oman.domain.auth.dto.AuthResponse;
import com.oman.domain.auth.service.AuthService;
import com.oman.domain.member.entity.SocialProvider;
import com.oman.global.error.ErrorCode;
import com.oman.global.error.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/{provider}")
    public ResponseEntity<AuthResponse> login(
        @PathVariable String provider,
        @RequestBody Map<String, String> requestData) {

        String idToken = requestData.get("idToken");

        SocialProvider socialProvider = SocialProvider.valueOf(provider.toUpperCase());

        AuthResponse response = authService.login(socialProvider, idToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> requestData) {
        String refreshToken = requestData.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(ErrorCode.MISSING_REFRESH_TOKEN);
        }

        AuthResponse response = authService.reissueToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}