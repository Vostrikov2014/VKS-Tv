package com.jmp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for updating a tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUpdateRequest {

    private String name;

    private String domain;

    private Integer maxParticipants;

    private Integer maxDurationMinutes;

    private Integer maxRecordings;

    private Integer recordingRetentionDays;

    private String jitsiDomain;

    private Boolean jibriEnabled;

    private String s3Bucket;

    private String s3Region;

    private String subscriptionTier;

    private Instant subscriptionExpiresAt;
}
