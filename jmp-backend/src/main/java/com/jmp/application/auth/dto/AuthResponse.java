package com.jmp.application.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for authentication response containing JWT tokens.
 */
public record AuthResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,

        @JsonProperty("user_id")
        String userId,

        @JsonProperty("email")
        String email,

        @JsonProperty("role")
        String role,

        @JsonProperty("tenant_id")
        String tenantId
) {
    public AuthResponse {
        // Default token type is Bearer
        if (tokenType == null) {
            throw new IllegalArgumentException("Token type cannot be null");
        }
    }

    /**
     * Creates an AuthResponse with default Bearer token type and 15 minutes expiration.
     */
    public static AuthResponse of(String accessToken, String refreshToken, String userId, 
                                   String email, String role, String tenantId) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900L, // 15 minutes in seconds
                userId,
                email,
                role,
                tenantId
        );
    }
}
