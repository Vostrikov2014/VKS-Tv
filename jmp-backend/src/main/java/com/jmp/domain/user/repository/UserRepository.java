package com.jmp.domain.user.repository;

import com.jmp.domain.user.entity.User;
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
 * Spring Data JPA repository for User entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a user by email with tenant eagerly loaded.
     */
    @EntityGraph(attributePaths = {"tenant"})
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists by email (case-insensitive).
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Finds all users belonging to a specific tenant.
     */
    @EntityGraph(attributePaths = {"tenant"})
    List<User> findByTenantId(String tenantId);

    /**
     * Finds all users belonging to a specific tenant with pagination.
     */
    @EntityGraph(attributePaths = {"tenant"})
    Page<User> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Finds users by email and tenant ID.
     */
    Optional<User> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Counts the number of users in a tenant.
     */
    long countByTenantId(String tenantId);

    /**
     * Finds all users with a specific role in a tenant.
     */
    List<User> findByTenantIdAndRole(String tenantId, User.UserRole role);

    /**
     * Searches for users by name or email within a tenant.
     */
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchByTenantAndNameOrEmail(
            @Param("tenantId") String tenantId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Finds all non-deleted users (soft delete support).
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    /**
     * Finds users with failed login attempts exceeding threshold.
     */
    List<User> findByFailedLoginAttemptsGreaterThan(int threshold);
}
