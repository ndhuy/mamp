package com.microstock.security.web.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user) {

    public static TokenResponse of(String access, String refresh, long expiresIn, UserResponse user) {
        return new TokenResponse(access, refresh, "Bearer", expiresIn, user);
    }
}
