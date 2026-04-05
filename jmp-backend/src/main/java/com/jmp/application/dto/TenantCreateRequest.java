package com.jmp.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for creating a tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String domain;

    private Integer maxParticipants;

    private Integer maxDurationMinutes;

    private Integer maxRecordings;

    private Integer recordingRetentionDays;

    private String jitsiDomain;

    private boolean jibriEnabled;

    private String s3Bucket;

    private String s3Region;

    private String subscriptionTier;

    private Instant subscriptionExpiresAt;
}
