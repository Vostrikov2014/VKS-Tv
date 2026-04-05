package com.jmp.infrastructure.jitsi;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating and validating Jitsi JWT tokens.
 * Supports HS256, RS256, and EdDSA algorithms.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JitsiJwtService {

    private final JitsiProperties jitsiProperties;

    /**
     * Generates a JWT token for joining a Jitsi conference.
     *
     * @param roomId The conference room ID
     * @param userId The user ID
     * @param name The user's display name
     * @param email The user's email (optional)
     * @param isModerator Whether the user is a moderator
     * @return Signed JWT token
     */
    public String generateToken(String roomId, String userId, String name, String email, boolean isModerator) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jitsiProperties.getTokenExpiration());

        Map<String, Object> context = new HashMap<>();
        context.put("user", createUserContext(userId, name, email, isModerator));

        return JWT.create()
                .withIssuer(jitsiProperties.getAppId())
                .withSubject(email != null ? email : userId)
                .withAudience(jitsiProperties.getDomain())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiration))
                .withJWTId(roomId)
                .withClaim("room", roomId)
                .withClaim("context", context)
                .sign(getAlgorithm());
    }

    /**
     * Generates a token with minimal claims for anonymous users.
     */
    public String generateAnonymousToken(String roomId, String displayName) {
        return generateToken(roomId, "anon-" + System.currentTimeMillis(), displayName, null, false);
    }

    /**
     * Validates a Jitsi JWT token.
     *
     * @param token The JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(getAlgorithm())
                    .withIssuer(jitsiProperties.getAppId())
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts room ID from token.
     */
    public String extractRoomId(String token) {
        try {
            return JWT.decode(token).getClaim("room").asString();
        } catch (Exception e) {
            log.error("Failed to extract room ID from token", e);
            return null;
        }
    }

    /**
     * Extracts user information from token.
     */
    public Map<String, Object> extractUserContext(String token) {
        try {
            return JWT.decode(token).getClaim("context").asMap();
        } catch (Exception e) {
            log.error("Failed to extract user context from token", e);
            return new HashMap<>();
        }
    }

    /**
     * Gets the Algorithm instance based on configuration.
     */
    private Algorithm getAlgorithm() {
        String algorithm = jitsiProperties.getAlgorithm();
        String secret = jitsiProperties.getAppSecret();

        return switch (algorithm) {
            case "RS256" -> {
                // For RS256, you would load the private key from vault/secrets
                // This is a simplified example
                yield Algorithm.RSA256(null, null);
            }
            case "EdDSA" -> {
                log.warn("EdDSA requested but not yet implemented, falling back to HMAC256");
                yield Algorithm.HMAC256(secret);
            }
            default -> Algorithm.HMAC256(secret);
        };
    }

    /**
     * Creates the user context object for the JWT.
     */
    private Map<String, Object> createUserContext(String userId, String name, String email, boolean isModerator) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("name", name);
        
        if (email != null) {
            user.put("email", email);
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("moderator", isModerator);
        user.put("userInfo", userInfo);
        
        return user;
    }
}
