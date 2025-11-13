package com.dansmultipro.ops.util;

import com.dansmultipro.ops.exception.BlacklistedTokenException;
import com.dansmultipro.ops.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final StringRedisTemplate redis;

    @Value("${security.jwt.secret}")
    private String secretValue;

    @Value("${security.jwt.expMinutes}")
    private long expMinutes;

    public JwtUtil(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private SecretKey setSecretKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretValue);
        } catch (IllegalArgumentException ex) {
            keyBytes = secretValue.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofMinutes(expMinutes));

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().getCode())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(setSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        if (isBlacklisted(token)) {
            throw new BlacklistedTokenException("Token has been revoked");
        }
        return Jwts.parserBuilder()
                .setSigningKey(setSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void blacklist(String token) {
        Claims c = parseClaims(token);
        long ttlMs = c.getExpiration().getTime() - System.currentTimeMillis();

        if (ttlMs <= 0) return;

        String key = "jwt:bl:" + token;
        redis.opsForValue().set(key, "1", Duration.ofMillis(ttlMs));
    }

    public boolean isBlacklisted(String token) {
        String key = "jwt:bl:" + token;
        return redis.hasKey(key);
    }
}
