package com.jmp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for recording data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingDto {

    private String id;
    private String title;
    private String description;
    private String status;
    private String recordingType;
    private String s3Bucket;
    private String s3Key;
    private String s3Url;
    private String downloadUrl;
    private Long fileSizeBytes;
    private Long durationSeconds;
    private String mimeType;
    private String thumbnailUrl;
    private boolean encrypted;
    private Instant startedAt;
    private Instant completedAt;
    private String errorMessage;
    private int retryCount;
    private Instant retentionUntil;
    private boolean legalHold;
    private int downloadCount;
    private String conferenceId;
    private Instant createdAt;
    private Instant updatedAt;
}
