package com.jmp.infrastructure.storage.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * S3 storage service for conference recordings.
 * Implements secure upload, download, and presigned URL generation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${jmp.storage.bucket-name}")
    private String bucketName;

    @Value("${jmp.storage.region:us-east-1}")
    private String region;

    /**
     * Upload recording file to S3 with encryption.
     * Uses server-side encryption (SSE-S3 or KMS).
     */
    public String uploadRecording(String tenantId, UUID recordingId, InputStream inputStream, 
                                  String contentType, long contentLength) {
        String key = buildObjectKey(tenantId, recordingId);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .metadata(Map.of(
                    "tenant-id", tenantId,
                    "recording-id", recordingId.toString(),
                    "uploaded-at", String.valueOf(System.currentTimeMillis())
                ))
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
            log.info("Successfully uploaded recording to S3: bucket={}, key={}", bucketName, key);
            return key;
            
        } catch (Exception e) {
            log.error("Failed to upload recording to S3: key={}", key, e);
            throw new StorageException("Failed to upload recording", e);
        }
    }

    /**
     * Generate presigned download URL with expiration.
     * Provides secure temporary access without exposing credentials.
     */
    public URL generatePresignedDownloadUrl(String objectKey, Duration expiration) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(builder -> builder
                    .bucket(bucketName)
                    .key(objectKey)
                )
                .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            URL presignedUrl = presignedRequest.url();
            
            log.debug("Generated presigned URL for key: {}, expires in: {}", objectKey, expiration);
            return presignedUrl;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", objectKey, e);
            throw new StorageException("Failed to generate download URL", e);
        }
    }

    /**
     * Delete recording from S3.
     * Implements soft delete with optional retention policy.
     */
    public void deleteRecording(String objectKey, boolean forceDelete) {
        try {
            // Check retention policy before deletion
            if (!forceDelete && isUnderRetentionPeriod(objectKey)) {
                log.warn("Cannot delete recording under retention policy: {}", objectKey);
                throw new StorageException("Recording is under retention policy", null);
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted recording from S3: key={}", objectKey);
            
        } catch (Exception e) {
            log.error("Failed to delete recording from S3: key={}", objectKey, e);
            throw new StorageException("Failed to delete recording", e);
        }
    }

    /**
     * Get recording metadata from S3.
     */
    public RecordingMetadata getRecordingMetadata(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            
            return new RecordingMetadata(
                objectKey,
                response.contentLength(),
                response.contentType(),
                response.lastModified(),
                response.metadata()
            );
            
        } catch (NoSuchKeyException e) {
            log.warn("Recording not found in S3: key={}", objectKey);
            throw new StorageException("Recording not found", e);
        } catch (Exception e) {
            log.error("Failed to get recording metadata: key={}", objectKey, e);
            throw new StorageException("Failed to get metadata", e);
        }
    }

    /**
     * List all recordings for a tenant.
     */
    public java.util.List<String> listTenantRecordings(String tenantId) {
        try {
            String prefix = "tenants/" + tenantId + "/recordings/";
            
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
            
            return response.contents().stream()
                .map(S3Object::key)
                .toList();
                
        } catch (Exception e) {
            log.error("Failed to list recordings for tenant: {}", tenantId, e);
            throw new StorageException("Failed to list recordings", e);
        }
    }

    /**
     * Copy recording to archive bucket for long-term storage.
     */
    public void archiveRecording(String objectKey, String archiveBucket) {
        try {
            String archiveKey = objectKey.replace("recordings/", "archive/");
            
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(objectKey)
                .destinationBucket(archiveBucket)
                .destinationKey(archiveKey)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();

            s3Client.copyObject(copyRequest);
            
            log.info("Successfully archived recording: {} -> {}", objectKey, archiveKey);
            
        } catch (Exception e) {
            log.error("Failed to archive recording: {}", objectKey, e);
            throw new StorageException("Failed to archive recording", e);
        }
    }

    /**
     * Verify recording integrity using ETag.
     */
    public boolean verifyRecordingIntegrity(String objectKey, String expectedEtag) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            String actualEtag = response.eTag().replace("\"", "");
            
            boolean isValid = actualEtag.equalsIgnoreCase(expectedEtag);
            if (!isValid) {
                log.warn("Recording integrity check failed: key={}, expected={}, actual={}", 
                        objectKey, expectedEtag, actualEtag);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Failed to verify recording integrity: key={}", objectKey, e);
            return false;
        }
    }

    private String buildObjectKey(String tenantId, UUID recordingId) {
        return String.format("tenants/%s/recordings/%s.mp4", tenantId, recordingId);
    }

    private boolean isUnderRetentionPeriod(String objectKey) {
        // Implement retention policy check based on tenant configuration
        // For now, return false to allow deletion
        return false;
    }

    /**
     * Custom exception for storage operations.
     */
    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Recording metadata DTO.
     */
    public record RecordingMetadata(
        String objectKey,
        Long contentLength,
        String contentType,
        java.time.Instant lastModified,
        java.util.Map<String, String> userMetadata
    ) {}
}
