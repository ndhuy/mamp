package com.microstock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds app.security.* settings. */
@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        String jwtSecret,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays,
        String issuer) {
}
