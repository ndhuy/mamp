package com.microstock.security.service;

import com.microstock.common.domain.Role;
import com.microstock.common.security.AppUserPrincipal;
import com.microstock.config.AppSecurityProperties;
import com.microstock.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Issues and validates stateless HS256 access tokens. */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;

    public JwtService(AppSecurityProperties props) {
        this.key = Keys.hmacShaKeyFor(props.jwtSecret().getBytes(StandardCharsets.UTF_8));
        this.issuer = props.issuer();
        this.accessTtl = Duration.ofMinutes(props.accessTokenTtlMinutes());
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    /** Parses and verifies a token, returning the principal, or throws on failure. */
    public AppUserPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AppUserPrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("username", String.class),
                Role.valueOf(claims.get("role", String.class)));
    }
}
