package com.jmp.domain.recording.repository;

import com.jmp.domain.recording.entity.Recording;
import com.jmp.domain.recording.entity.Recording.RecordingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

/**
 * Repository interface for Recording entity operations.
 * Provides methods for querying recordings by various criteria.
 */
@Repository
public interface RecordingRepository extends JpaRepository<Recording, String> {

    /**
     * Finds a recording by ID with conference eagerly loaded.
     */
    @EntityGraph(attributePaths = {"conference"})
    Optional<Recording> findById(String id);

    /**
     * Finds all recordings for a specific conference.
     */
    @EntityGraph(attributePaths = {"conference"})
    List<Recording> findByConferenceId(String conferenceId);

    /**
     * Finds all recordings for a specific conference with pagination.
     */
    @EntityGraph(attributePaths = {"conference"})
    Page<Recording> findByConferenceId(String conferenceId, Pageable pageable);

    /**
     * Finds recordings by status.
     */
    List<Recording> findByStatus(RecordingStatus status);

    /**
     * Finds recordings that are ready for playback.
     */
    @EntityGraph(attributePaths = {"conference"})
    Page<Recording> findByStatus(RecordingStatus status, Pageable pageable);

    /**
     * Finds recordings that have passed their retention date and are not on legal hold.
     */
    @Query("SELECT r FROM Recording r WHERE r.retentionUntil < :now AND r.legalHold = false AND r.status NOT IN :excludedStatuses")
    List<Recording> findPastRetentionDate(@Param("now") Instant now, @Param("excludedStatuses") List<RecordingStatus> excludedStatuses);

    /**
     * Counts recordings by conference ID.
     */
    long countByConferenceId(String conferenceId);

    /**
     * Counts recordings by tenant ID through conference relationship.
     */
    @Query("SELECT COUNT(r) FROM Recording r JOIN r.conference c WHERE c.tenant.id = :tenantId")
    long countByTenantId(@Param("tenantId") String tenantId);

    /**
     * Finds recordings by tenant ID with pagination.
     */
    @Query("SELECT r FROM Recording r JOIN r.conference c WHERE c.tenant.id = :tenantId ORDER BY r.createdAt DESC")
    Page<Recording> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Finds recordings by title containing search term (case-insensitive).
     */
    @Query("SELECT r FROM Recording r JOIN r.conference c WHERE c.tenant.id = :tenantId AND LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY r.createdAt DESC")
    Page<Recording> searchByTitle(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Finds failed recordings that can be retried.
     */
    @Query("SELECT r FROM Recording r WHERE r.status = 'FAILED' AND r.retryCount < :maxRetries ORDER BY r.updatedAt ASC")
    List<Recording> findFailedRecordingsForRetry(@Param("maxRetries") int maxRetries);

    /**
     * Updates recording status atomically.
     */
    @Modifying
    @Query("UPDATE Recording r SET r.status = :status, r.updatedAt = :updatedAt WHERE r.id = :id")
    int updateStatus(@Param("id") String id, @Param("status") RecordingStatus status, @Param("updatedAt") Instant updatedAt);

    /**
     * Marks recording as deleted (soft delete).
     */
    @Modifying
    @Query("UPDATE Recording r SET r.deletedAt = :deletedAt WHERE r.id = :id")
    int markDeleted(@Param("id") String id, @Param("deletedAt") Instant deletedAt);

    /**
     * Locks a recording for update to prevent concurrent modifications.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Recording> findWithLockById(String id);

    /**
     * Finds total size of recordings by tenant.
     */
    @Query("SELECT COALESCE(SUM(r.fileSizeBytes), 0) FROM Recording r JOIN r.conference c WHERE c.tenant.id = :tenantId AND r.status = 'READY'")
    Long getTotalSizeByTenantId(@Param("tenantId") String tenantId);

    /**
     * Finds recordings created within a date range.
     */
    @Query("SELECT r FROM Recording r JOIN r.conference c WHERE c.tenant.id = :tenantId AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    Page<Recording> findByDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );

    /**
     * Deletes recordings that are marked as deleted and past the cleanup threshold.
     */
    @Modifying
    @Query("DELETE FROM Recording r WHERE r.deletedAt IS NOT NULL AND r.deletedAt < :threshold")
    int deletePermanently(@Param("threshold") Instant threshold);
}
