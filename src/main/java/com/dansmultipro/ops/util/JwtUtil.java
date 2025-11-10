package com.dansmultipro.ops.util;

import com.dansmultipro.ops.dto.auth.TokenPair;
import com.dansmultipro.ops.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final String secretValue;
    private final Duration expiration;
    private SecretKey secretKey;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secretValue,
            @Value("${security.jwt.expMinutes}") long expMinutes) {
        this.secretValue = secretValue;
        this.expiration = Duration.ofMinutes(expMinutes);
    }

    @PostConstruct
    void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretValue);
        } catch (IllegalArgumentException ex) {
            keyBytes = secretValue.getBytes(StandardCharsets.UTF_8);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenPair generateToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().getCode())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return new TokenPair(token, expiresAt);
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException ex) {
            throw new JwtException("Invalid token provided");
        }
    }
}
