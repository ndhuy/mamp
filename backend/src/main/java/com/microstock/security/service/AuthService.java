package com.microstock.security.service;

import com.microstock.common.domain.Role;
import com.microstock.common.error.ApiException;
import com.microstock.config.AppSecurityProperties;
import com.microstock.security.domain.RefreshToken;
import com.microstock.security.repository.RefreshTokenRepository;
import com.microstock.security.web.dto.*;
import com.microstock.user.domain.User;
import com.microstock.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Duration refreshTtl;
    private final long accessTtlSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppSecurityProperties props) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTtl = Duration.ofDays(props.refreshTokenTtlDays());
        this.accessTtlSeconds = Duration.ofMinutes(props.accessTokenTtlMinutes()).toSeconds();
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw ApiException.badRequest("Password and confirmation do not match"); // VAL-003
        }
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("Email is already registered"); // VAL-001
        }
        if (userRepository.existsByUsernameIgnoreCase(req.username())) {
            throw ApiException.conflict("Username is already taken"); // VAL-002
        }
        User user = new User(
                req.email().trim(),
                req.username().trim(),
                passwordEncoder.encode(req.password()),
                Role.USER);
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        String id = req.identifier().trim();
        User user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(id, id)
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid credentials");
        }
        if (!user.isActive()) {
            throw ApiException.unauthorized("Account is disabled"); // AUTH-004, BR-019
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        String hash = sha256(req.refreshToken());
        RefreshToken stored = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));
        if (!stored.isActive()) {
            throw ApiException.unauthorized("Refresh token expired or revoked");
        }
        User user = userRepository
                .findById(stored.getUserId())
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));
        if (!user.isActive()) {
            throw ApiException.unauthorized("Account is disabled");
        }
        // Rotate: revoke the presented token, issue a fresh pair.
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw ApiException.badRequest("Password and confirmation do not match");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.unauthorized("No user"));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        // Invalidate existing sessions after a password change.
        refreshTokenRepository.revokeAllForUser(userId);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(sha256(refreshToken)).ifPresent(t -> {
            t.setRevokedAt(Instant.now());
            refreshTokenRepository.save(t);
        });
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.issueAccessToken(user);
        String rawRefresh = generateRefreshToken();
        RefreshToken token = new RefreshToken(
                user.getId(), sha256(rawRefresh), Instant.now().plus(refreshTtl));
        refreshTokenRepository.save(token);
        return TokenResponse.of(accessToken, rawRefresh, accessTtlSeconds, UserResponse.from(user));
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
