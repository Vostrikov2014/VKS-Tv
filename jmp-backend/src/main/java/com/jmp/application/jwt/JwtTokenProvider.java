package com.jmp.application.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 * 
 * Implements secure token generation with support for custom claims
 * required by Jitsi integration (room, moderator, tenant_id).
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:900000}") long expirationMs,
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs,
            @Value("${jwt.issuer:jmp-platform}") String issuer) {
        
        // Ensure secret is at least 256 bits for HS256
        if (secret.length() < 32) {
            log.warn("JWT secret is too short. Using padded secret for security.");
            secret = secret + "0".repeat(32 - secret.length());
        }
        
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
    }

    /**
     * Generates an access token with standard and custom claims.
     * 
     * @param subject User identifier (email or ID)
     * @param userId User UUID
     * @param tenantId Tenant identifier
     * @param role User role
     * @param roomId Optional room ID for conference-specific tokens
     * @param isModerator Whether the user is a moderator
     * @return JWT access token
     */
    public String generateAccessToken(String subject, String userId, String tenantId, 
                                       String role, String roomId, boolean isModerator) {
        Instant now = Instant.now();
        Instant expireAt = now.plusMillis(expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("tenant_id", tenantId);
        claims.put("role", role);
        
        // Jitsi-specific claims
        if (roomId != null) {
            claims.put("room", roomId);
            claims.put("mod", isModerator);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .issuer(issuer)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a refresh token with minimal claims.
     */
    public String generateRefreshToken(String subject, String userId) {
        Instant now = Instant.now();
        Instant expireAt = now.plusMillis(refreshExpirationMs);

        return Jwts.builder()
                .subject(subject)
                .claim("user_id", userId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .issuer(issuer)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a token and returns true if it's valid and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the subject (email/userId) from the token.
     */
    public String getSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if the token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Instant expiration = extractClaim(token, Claims::getExpiration).toInstant();
            return expiration.isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extracts the user ID claim from the token.
     */
    public String getUserId(String token) {
        return extractClaim(token, claims -> claims.get("user_id", String.class));
    }

    /**
     * Extracts the tenant ID claim from the token.
     */
    public String getTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenant_id", String.class));
    }

    /**
     * Extracts the role claim from the token.
     */
    public String getRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extracts the room claim from the token (Jitsi-specific).
     */
    public String getRoom(String token) {
        return extractClaim(token, claims -> claims.get("room", String.class));
    }

    /**
     * Extracts the moderator flag from the token (Jitsi-specific).
     */
    public Boolean isModerator(String token) {
        return extractClaim(token, claims -> claims.get("mod", Boolean.class));
    }

    /**
     * Gets the token type (access or refresh).
     */
    public String getTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
}
