package com.jmp.domain.recording.entity;

import com.jmp.domain.common.BaseEntity;
import com.jmp.domain.conference.entity.Conference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

/**
 * Recording entity representing a video conference recording.
 * Stores metadata about recordings captured by Jibri.
 */
@Entity
@Table(name = "recordings")
public class Recording extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordingStatus status = RecordingStatus.PENDING;

    @Column(name = "recording_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordingType recordingType = RecordingType.FILE;

    @Column(name = "s3_bucket")
    private String s3Bucket;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "s3_url")
    private String s3Url;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "hash_sha256")
    private String hashSha256;

    @Column(name = "encrypted", nullable = false)
    private boolean encrypted = true;

    @Column(name = "encryption_key_id")
    private String encryptionKeyId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "retention_until")
    private Instant retentionUntil;

    @Column(name = "legal_hold", nullable = false)
    private boolean legalHold = false;

    @Column(name = "download_count", nullable = false)
    private int downloadCount = 0;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    public enum RecordingStatus {
        PENDING,
        RECORDING,
        PROCESSING,
        READY,
        FAILED,
        DELETED,
        ARCHIVED
    }

    public enum RecordingType {
        FILE,
        STREAM
    }

    // Constructors
    public Recording() {
    }

    public Recording(String title, Conference conference) {
        this.title = title;
        this.conference = conference;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecordingStatus getStatus() {
        return status;
    }

    public void setStatus(RecordingStatus status) {
        this.status = status;
    }

    public RecordingType getRecordingType() {
        return recordingType;
    }

    public void setRecordingType(RecordingType recordingType) {
        this.recordingType = recordingType;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public void setHashSha256(String hashSha256) {
        this.hashSha256 = hashSha256;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }

    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getRetentionUntil() {
        return retentionUntil;
    }

    public void setRetentionUntil(Instant retentionUntil) {
        this.retentionUntil = retentionUntil;
    }

    public boolean isLegalHold() {
        return legalHold;
    }

    public void setLegalHold(boolean legalHold) {
        this.legalHold = legalHold;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    /**
     * Marks the recording as ready for playback.
     */
    public void markReady() {
        this.status = RecordingStatus.READY;
        this.completedAt = Instant.now();
        if (this.startedAt != null) {
            this.durationSeconds = Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    /**
     * Marks the recording as failed with an error message.
     */
    public void markFailed(String error) {
        this.status = RecordingStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = Instant.now();
    }

    /**
     * Increments the download count.
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * Calculates the retention date based on tenant policy.
     */
    public void calculateRetentionDate(int retentionDays) {
        this.retentionUntil = Instant.now().plusSeconds(retentionDays * 86400L);
    }

    /**
     * Checks if the recording is past its retention date.
     */
    public boolean isPastRetention() {
        if (this.legalHold) {
            return false;
        }
        return this.retentionUntil != null && Instant.now().isAfter(this.retentionUntil);
    }
}
