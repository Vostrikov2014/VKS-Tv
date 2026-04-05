package com.jmp.domain.conference.repository;

import com.jmp.domain.conference.entity.Conference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Conference entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ConferenceRepository extends JpaRepository<Conference, String> {

    /**
     * Finds a conference by room ID.
     */
    Optional<Conference> findByRoomId(String roomId);

    /**
     * Checks if a conference exists by room ID.
     */
    boolean existsByRoomId(String roomId);

    /**
     * Finds all conferences belonging to a tenant.
     */
    @EntityGraph(attributePaths = {"tenant", "createdBy"})
    List<Conference> findByTenantId(String tenantId);

    /**
     * Finds all conferences belonging to a tenant with pagination.
     */
    @EntityGraph(attributePaths = {"tenant", "createdBy"})
    Page<Conference> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Finds conferences by status and tenant.
     */
    @EntityGraph(attributePaths = {"tenant", "createdBy"})
    List<Conference> findByTenantIdAndStatus(String tenantId, Conference.ConferenceStatus status);

    /**
     * Finds scheduled conferences starting within a time range.
     */
    @Query("SELECT c FROM Conference c WHERE c.tenant.id = :tenantId " +
           "AND c.scheduledStartAt BETWEEN :start AND :end")
    List<Conference> findScheduledInRange(
            @Param("tenantId") String tenantId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Searches conferences by name within a tenant.
     */
    @Query("SELECT c FROM Conference c WHERE c.tenant.id = :tenantId " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Conference> searchByTenantAndName(
            @Param("tenantId") String tenantId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Counts conferences by tenant and status.
     */
    long countByTenantIdAndStatus(String tenantId, Conference.ConferenceStatus status);

    /**
     * Counts all conferences by tenant.
     */
    long countByTenantId(String tenantId);

    /**
     * Finds conferences created by a specific user.
     */
    @EntityGraph(attributePaths = {"tenant", "createdBy"})
    List<Conference> findByCreatedById(String userId);

    /**
     * Finds conferences ending before a specific time (for cleanup).
     */
    @Query("SELECT c FROM Conference c WHERE c.status = 'ACTIVE' " +
           "AND c.scheduledEndAt < :now")
    List<Conference> findEndedConferences(@Param("now") Instant now);

    /**
     * Finds all non-deleted conferences.
     */
    @Query("SELECT c FROM Conference c WHERE c.deletedAt IS NULL")
    Page<Conference> findAllActive(Pageable pageable);

    /**
     * Finds conferences by status and scheduled start time range.
     */
    List<Conference> findByStatusAndScheduledStartAtBetween(Conference.ConferenceStatus status, Instant start, Instant end);

    /**
     * Finds conferences by status and ended before a specific time.
     */
    List<Conference> findByStatusAndEndedAtBefore(Conference.ConferenceStatus status, Instant instant);

    /**
     * Counts conferences created within a time range.
     */
    long countByCreatedAtBetween(Instant start, Instant end);

    /**
     * Counts conferences by status and created within a time range.
     */
    long countByStatusAndCreatedAtBetween(Conference.ConferenceStatus status, Instant start, Instant end);
}
