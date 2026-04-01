package com.zxy.aiplanner.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具类（不依赖 Spring Security）
 */
@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expireMs;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expire-ms}") long expireMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = expireMs;
    }

    public String generateToken(Long userId, String loginName) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expireMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("loginName", loginName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }

    public boolean isExpired(String token) {
        Claims claims = parseClaims(token);
        Date exp = claims.getExpiration();
        return exp == null || exp.before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return false;
            }
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

