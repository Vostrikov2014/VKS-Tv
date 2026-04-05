package com.jmp.domain.audit.repository;

import com.jmp.domain.audit.entity.AuditLog;
import com.jmp.domain.audit.entity.AuditLog.AuditEventType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for AuditLog entity operations.
 * Provides methods for querying audit logs with various filters for compliance and forensics.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    /**
     * Finds audit logs by user ID with pagination.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    /**
     * Finds audit logs by tenant ID with pagination.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    Page<AuditLog> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Finds audit logs by event type with pagination.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    Page<AuditLog> findByEventType(AuditEventType eventType, Pageable pageable);

    /**
     * Finds audit logs by event type and tenant ID with pagination.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    Page<AuditLog> findByEventTypeAndTenantId(AuditEventType eventType, String tenantId, Pageable pageable);

    /**
     * Finds audit logs within a date range for a specific tenant.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.tenant.id = :tenantId AND a.occurredAt BETWEEN :startDate AND :endDate ORDER BY a.occurredAt DESC")
    Page<AuditLog> findByTenantIdAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );

    /**
     * Finds audit logs by action containing search term (case-insensitive).
     */
    @Query("SELECT a FROM AuditLog a WHERE a.tenant.id = :tenantId AND LOWER(a.action) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY a.occurredAt DESC")
    Page<AuditLog> searchByAction(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Finds audit logs by resource type and ID.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId);

    /**
     * Finds failed login attempts for a specific IP address within a time window.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.eventType = 'LOGIN_FAILURE' AND a.occurredAt > :since")
    long countFailedLoginsByIpAddress(@Param("ipAddress") String ipAddress, @Param("since") Instant since);

    /**
     * Finds failed login attempts for a specific user within a time window.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId AND a.eventType = 'LOGIN_FAILURE' AND a.occurredAt > :since")
    long countFailedLoginsByUserId(@Param("userId") String userId, @Param("since") Instant since);

    /**
     * Finds suspicious activity events within a time range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType IN ('SUSPICIOUS_ACTIVITY', 'ACCESS_DENIED', 'RATE_LIMIT_EXCEEDED') AND a.occurredAt > :since ORDER BY a.occurredAt DESC")
    List<AuditLog> findSuspiciousActivity(@Param("since") Instant since);

    /**
     * Finds all login failure events for security analysis.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    Page<AuditLog> findByEventTypeIn(List<AuditEventType> eventTypes, Pageable pageable);

    /**
     * Counts audit logs by event type for a tenant.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.tenant.id = :tenantId AND a.eventType = :eventType")
    long countByTenantIdAndEventType(@Param("tenantId") String tenantId, @Param("eventType") AuditEventType eventType);

    /**
     * Finds audit logs by correlation ID for tracing a request across multiple events.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    List<AuditLog> findByCorrelationIdOrderByOccurredAtAsc(String correlationId);

    /**
     * Deletes audit logs older than the specified threshold (for data retention policies).
     * Note: In production, consider making audit logs immutable or using soft delete.
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.occurredAt < :threshold")
    int deleteOlderThan(@Param("threshold") Instant threshold);

    /**
     * Finds audit logs by trace ID for distributed tracing.
     */
    @EntityGraph(attributePaths = {"user", "tenant"})
    List<AuditLog> findByTraceIdOrderByOccurredAtAsc(String traceId);

    /**
     * Finds recent audit logs for dashboard display.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.tenant.id = :tenantId ORDER BY a.occurredAt DESC")
    Page<AuditLog> findRecentLogs(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Exports audit logs for compliance reporting.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.tenant.id = :tenantId AND a.occurredAt BETWEEN :startDate AND :endDate ORDER BY a.occurredAt ASC")
    List<AuditLog> exportForCompliance(
        @Param("tenantId") String tenantId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}
