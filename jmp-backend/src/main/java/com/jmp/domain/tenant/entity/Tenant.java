package com.jmp.domain.tenant.entity;

import com.jmp.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Tenant entity representing an organization in the multi-tenant architecture.
 * Each tenant has isolated data, configurations, and quotas.
 */
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "domain")
    private String domain;

    @Column(name = "status", nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "max_participants")
    private Integer maxParticipants = 100;

    @Column(name = "max_duration_minutes")
    private Integer maxDurationMinutes = 480;

    @Column(name = "max_recordings")
    private Integer maxRecordings = 50;

    @Column(name = "recording_retention_days")
    private Integer recordingRetentionDays = 90;

    @Column(name = "jitsi_domain")
    private String jitsiDomain;

    @Column(name = "jibri_enabled")
    private boolean jibriEnabled = false;

    @Column(name = "s3_bucket")
    private String s3Bucket;

    @Column(name = "s3_region")
    private String s3Region;

    @Column(name = "subscription_tier")
    private String subscriptionTier = "FREE";

    @Column(name = "subscription_expires_at")
    private Instant subscriptionExpiresAt;

    public enum TenantStatus {
        ACTIVE,
        SUSPENDED,
        PENDING,
        DELETED
    }

    // Constructors
    public Tenant() {
    }

    public Tenant(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getMaxDurationMinutes() {
        return maxDurationMinutes;
    }

    public void setMaxDurationMinutes(Integer maxDurationMinutes) {
        this.maxDurationMinutes = maxDurationMinutes;
    }

    public Integer getMaxRecordings() {
        return maxRecordings;
    }

    public void setMaxRecordings(Integer maxRecordings) {
        this.maxRecordings = maxRecordings;
    }

    public Integer getRecordingRetentionDays() {
        return recordingRetentionDays;
    }

    public void setRecordingRetentionDays(Integer recordingRetentionDays) {
        this.recordingRetentionDays = recordingRetentionDays;
    }

    public String getJitsiDomain() {
        return jitsiDomain;
    }

    public void setJitsiDomain(String jitsiDomain) {
        this.jitsiDomain = jitsiDomain;
    }

    public boolean isJibriEnabled() {
        return jibriEnabled;
    }

    public void setJibriEnabled(boolean jibriEnabled) {
        this.jibriEnabled = jibriEnabled;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Region() {
        return s3Region;
    }

    public void setS3Region(String s3Region) {
        this.s3Region = s3Region;
    }

    public String getSubscriptionTier() {
        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }

    public Instant getSubscriptionExpiresAt() {
        return subscriptionExpiresAt;
    }

    public void setSubscriptionExpiresAt(Instant subscriptionExpiresAt) {
        this.subscriptionExpiresAt = subscriptionExpiresAt;
    }

    /**
     * Checks if the tenant is active and can use the platform.
     */
    public boolean isActive() {
        return this.status == TenantStatus.ACTIVE;
    }

    /**
     * Suspends the tenant.
     */
    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    /**
     * Activates the tenant.
     */
    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }
}
