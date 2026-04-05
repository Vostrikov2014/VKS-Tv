package com.jmp.domain.audit.entity;

import com.jmp.domain.common.BaseEntity;
import com.jmp.domain.tenant.entity.Tenant;
import com.jmp.domain.user.entity.User;
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
import java.time.Instant;

/**
 * AuditLog entity for tracking all administrative and user actions.
 * Complies with GDPR/152-FZ requirements for immutable audit trails.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_method")
    private String requestMethod;

    @Column(name = "request_uri")
    private String requestUri;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "success", nullable = false)
    private boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "span_id")
    private String spanId;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public enum AuditEventType {
        // Authentication events
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED,
        
        // User management events
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED,
        USER_SUSPENDED,
        USER_ACTIVATED,
        ROLE_CHANGED,
        
        // Tenant management events
        TENANT_CREATED,
        TENANT_UPDATED,
        TENANT_SUSPENDED,
        TENANT_ACTIVATED,
        TENANT_DELETED,
        
        // Conference events
        CONFERENCE_CREATED,
        CONFERENCE_UPDATED,
        CONFERENCE_DELETED,
        CONFERENCE_STARTED,
        CONFERENCE_ENDED,
        CONFERENCE_CANCELLED,
        PARTICIPANT_JOINED,
        PARTICIPANT_LEFT,
        KICKED_PARTICIPANT,
        MUTED_PARTICIPANT,
        
        // Recording events
        RECORDING_STARTED,
        RECORDING_STOPPED,
        RECORDING_DOWNLOADED,
        RECORDING_DELETED,
        RECORDING_SHARED,
        
        // Security events
        ACCESS_DENIED,
        RATE_LIMIT_EXCEEDED,
        SUSPICIOUS_ACTIVITY,
        SESSION_EXPIRED,
        TOKEN_REVOKED,
        
        // System events
        CONFIGURATION_CHANGED,
        SYSTEM_STARTUP,
        SYSTEM_SHUTDOWN,
        BACKUP_COMPLETED,
        MAINTENANCE_STARTED,
        MAINTENANCE_COMPLETED
    }

    // Constructors
    public AuditLog() {
    }

    public AuditLog(AuditEventType eventType, String action, String description) {
        this.eventType = eventType;
        this.action = action;
        this.description = description;
        this.occurredAt = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    /**
     * Marks the audit log as failed with an error message.
     */
    public void markFailed(String error) {
        this.success = false;
        this.errorMessage = error;
    }

    /**
     * Sanitizes sensitive data from request/response bodies.
     * Should be called before persisting to avoid storing passwords, tokens, etc.
     */
    public void sanitize() {
        if (this.requestBody != null) {
            this.requestBody = sanitizeSensitiveData(this.requestBody);
        }
        if (this.responseBody != null) {
            this.responseBody = sanitizeSensitiveData(this.responseBody);
        }
    }

    /**
     * Basic sanitization to remove common sensitive fields.
     * In production, use a more sophisticated JSON sanitizer.
     */
    private String sanitizeSensitiveData(String json) {
        if (json == null) {
            return null;
        }
        return json
            .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***REDACTED***\"")
            .replaceAll("(?i)\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***REDACTED***\"")
            .replaceAll("(?i)\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***REDACTED***\"")
            .replaceAll("(?i)\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"***REDACTED***\"")
            .replaceAll("(?i)\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"***REDACTED***\"");
    }
}
