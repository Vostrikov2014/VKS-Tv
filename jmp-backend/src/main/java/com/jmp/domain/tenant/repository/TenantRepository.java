package com.jmp.domain.tenant.repository;

import com.jmp.domain.tenant.entity.Tenant;
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
 * Spring Data JPA repository for Tenant entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    /**
     * Finds a tenant by slug (unique identifier).
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Checks if a tenant exists by slug.
     */
    boolean existsBySlug(String slug);

    /**
     * Finds a tenant by name.
     */
    Optional<Tenant> findByName(String name);

    /**
     * Checks if a tenant exists by name.
     */
    boolean existsByName(String name);

    /**
     * Finds all tenants with a specific status.
     */
    List<Tenant> findByStatus(Tenant.TenantStatus status);

    /**
     * Finds all active tenants.
     */
    @EntityGraph(attributePaths = {})
    Page<Tenant> findByStatus(Tenant.TenantStatus status, Pageable pageable);

    /**
     * Searches for tenants by name or domain.
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.domain) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tenant> searchByNameOrDomain(
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Counts tenants by subscription tier.
     */
    long countBySubscriptionTier(String tier);

    /**
     * Finds tenants with expiring subscriptions within days.
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionExpiresAt IS NOT NULL " +
           "AND t.subscriptionExpiresAt <= CURRENT_TIMESTAMP + :days")
    List<Tenant> findExpiringSubscriptions(@Param("days") java.time.Duration days);

    /**
     * Finds all non-deleted tenants.
     */
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NULL")
    Page<Tenant> findAllActive(Pageable pageable);
}
