package com.jmp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for tenant data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {

    private String id;
    private String name;
    private String slug;
    private String domain;
    private String status;
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
    private Instant createdAt;
    private Instant updatedAt;
}
