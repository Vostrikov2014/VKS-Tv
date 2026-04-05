package com.jmp.infrastructure.jitsi;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for Jitsi integration.
 */
@Component
@ConfigurationProperties(prefix = "jitsi")
@Validated
@Slf4j
@Data
public class JitsiProperties {

    /**
     * Jitsi domain (e.g., meet.jit.si)
     */
    @NotBlank(message = "Jitsi domain is required")
    private String domain = "meet.jit.si";

    /**
     * JWT app ID for token generation
     */
    @NotBlank(message = "JWT app ID is required")
    private String appId = "";

    /**
     * JWT secret key for signing tokens
     */
    @NotBlank(message = "JWT secret is required")
    private String appSecret = "";

    /**
     * JWT algorithm (HS256, RS256, EdDSA)
     */
    private String algorithm = "HS256";

    /**
     * Token expiration time in seconds
     */
    private long tokenExpiration = 3600;

    /**
     * Jitsi REST API base URL
     */
    private String apiUrl = "";

    /**
     * Enable webhook signature verification
     */
    private boolean verifyWebhookSignature = true;

    /**
     * Webhook secret for HMAC verification
     */
    private String webhookSecret = "";

    /**
     * Connection timeout in milliseconds
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds
     */
    private int readTimeout = 10000;

    /**
     * Enable TLS/SSL for Jitsi connections
     */
    private boolean tlsEnabled = true;

    /**
     * Allowed IP addresses/ranges for webhook callbacks
     */
    private String[] allowedIps = new String[0];
}
