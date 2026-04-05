package com.jmp.application.user.service;

import com.jmp.domain.audit.entity.AuditLog;
import com.jmp.domain.audit.entity.AuditLog.AuditEventType;
import com.jmp.domain.audit.repository.AuditLogRepository;
import com.jmp.domain.tenant.entity.Tenant;
import com.jmp.domain.user.entity.User;
import com.jmp.domain.user.entity.User.UserRole;
import com.jmp.domain.user.entity.User.UserStatus;
import com.jmp.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing users in the platform.
 * Provides CRUD operations, role management, and security features.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by email (case-insensitive).
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Finds all users for a tenant with pagination.
     */
    public Page<User> findByTenantId(String tenantId, Pageable pageable) {
        return userRepository.findByTenantId(tenantId, pageable);
    }

    /**
     * Searches users by name or email within a tenant.
     */
    public Page<User> searchUsers(String tenantId, String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(tenantId, searchTerm, pageable);
    }

    /**
     * Creates a new user in the system.
     */
    @Transactional
    public User createUser(CreateUserRequest request, String currentUserId, Tenant tenant) {
        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.password());

        // Create user entity
        User user = new User();
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(encodedPassword);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole(request.role() != null ? request.role() : UserRole.USER);
        user.setStatus(UserStatus.PENDING);
        user.setTenant(tenant);

        // Save user
        User savedUser = userRepository.save(user);

        // Audit log
        logAuditEvent(
            AuditEventType.USER_CREATED,
            "User created",
            "USER",
            savedUser.getId(),
            "Created user: " + savedUser.getEmail(),
            currentUserId,
            tenant.getId(),
            true,
            null
        );

        log.info("Created user: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Updates an existing user.
     */
    @Transactional
    public User updateUser(String userId, UpdateUserRequest request, String currentUserId, Tenant tenant) {
        User user = userRepository.findByIdAndTenantId(userId, tenant.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update fields
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.role() != null) {
            UserRole oldRole = user.getRole();
            user.setRole(request.role());
            
            // Audit role change
            logAuditEvent(
                AuditEventType.ROLE_CHANGED,
                "Role changed",
                "USER",
                userId,
                "Role changed from " + oldRole + " to " + request.role(),
                currentUserId,
                tenant.getId(),
                true,
                null
            );
        }
        if (request.status() != null) {
            UserStatus oldStatus = user.getStatus();
            user.setStatus(request.status());
            
            // Audit status change
            if (request.status() == UserStatus.SUSPENDED) {
                logAuditEvent(
                    AuditEventType.USER_SUSPENDED,
                    "User suspended",
                    "USER",
                    userId,
                    "User suspended",
                    currentUserId,
                    tenant.getId(),
                    true,
                    null
                );
            } else if (request.status() == UserStatus.ACTIVE && oldStatus == UserStatus.SUSPENDED) {
                logAuditEvent(
                    AuditEventType.USER_ACTIVATED,
                    "User activated",
                    "USER",
                    userId,
                    "User activated",
                    currentUserId,
                    tenant.getId(),
                    true,
                    null
                );
            }
        }

        User updatedUser = userRepository.save(user);
        
        log.info("Updated user: {}", updatedUser.getEmail());
        return updatedUser;
    }

    /**
     * Soft deletes a user.
     */
    @Transactional
    public void deleteUser(String userId, String currentUserId, Tenant tenant) {
        User user = userRepository.findByIdAndTenantId(userId, tenant.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.markDeleted();
        userRepository.save(user);

        // Audit log
        logAuditEvent(
            AuditEventType.USER_DELETED,
            "User deleted",
            "USER",
            userId,
            "Deleted user: " + user.getEmail(),
            currentUserId,
            tenant.getId(),
            true,
            null
        );

        log.info("Deleted user: {}", user.getEmail());
    }

    /**
     * Activates a pending user.
     */
    @Transactional
    public User activateUser(String userId, String currentUserId, Tenant tenant) {
        User user = userRepository.findByIdAndTenantId(userId, tenant.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("User is not in PENDING status");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());

        User activatedUser = userRepository.save(user);

        // Audit log
        logAuditEvent(
            AuditEventType.USER_ACTIVATED,
            "User activated",
            "USER",
            userId,
            "User activated: " + activatedUser.getEmail(),
            currentUserId,
            tenant.getId(),
            true,
            null
        );

        log.info("Activated user: {}", activatedUser.getEmail());
        return activatedUser;
    }

    /**
     * Resets failed login attempts for a user.
     */
    @Transactional
    public void resetFailedLoginAttempts(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    /**
     * Records a successful login.
     */
    @Transactional
    public void recordLogin(String userId, String ipAddress, String userAgent) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            user.resetFailedLoginAttempts();
            userRepository.save(user);

            logAuditEvent(
                AuditEventType.LOGIN_SUCCESS,
                "Login successful",
                "USER",
                userId,
                "Successful login from IP: " + ipAddress,
                userId,
                user.getTenant().getId(),
                true,
                ipAddress,
                userAgent
            );
        });
    }

    /**
     * Records a failed login attempt.
     */
    @Transactional
    public void recordFailedLogin(String email, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementFailedLoginAttempts();
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.lockUntil(Instant.now().plusSeconds(900)); // 15 minutes lock
            }
            
            userRepository.save(user);

            logAuditEvent(
                AuditEventType.LOGIN_FAILURE,
                "Login failed",
                "USER",
                user.getId(),
                "Failed login attempt #" + user.getFailedLoginAttempts(),
                user.getId(),
                user.getTenant().getId(),
                false,
                "Invalid credentials",
                ipAddress,
                userAgent
            );
        } else {
            // Log failed attempt for non-existent user (potential enumeration attack)
            logAuditEvent(
                AuditEventType.LOGIN_FAILURE,
                "Login failed",
                "USER",
                null,
                "Failed login attempt for non-existent user: " + email,
                null,
                null,
                false,
                "User not found",
                ipAddress,
                userAgent
            );
        }
    }

    /**
     * Helper method to create audit log entries.
     */
    private void logAuditEvent(
        AuditEventType eventType,
        String action,
        String resourceType,
        String resourceId,
        String description,
        String userId,
        String tenantId,
        boolean success,
        String errorMessage
    ) {
        logAuditEvent(eventType, action, resourceType, resourceId, description, userId, tenantId, success, errorMessage, null, null);
    }

    private void logAuditEvent(
        AuditEventType eventType,
        String action,
        String resourceType,
        String resourceId,
        String description,
        String userId,
        String tenantId,
        boolean success,
        String errorMessage,
        String ipAddress,
        String userAgent
    ) {
        try {
            AuditLog auditLog = new AuditLog(eventType, action, description);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setSuccess(success);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setOccurredAt(Instant.now());

            if (userId != null) {
                userRepository.findById(userId).ifPresent(auditLog::setUser);
            }
            if (tenantId != null) {
                // In real implementation, fetch tenant
                // tenantRepository.findById(tenantId).ifPresent(auditLog::setTenant);
            }

            auditLog.sanitize();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log entry", e);
        }
    }

    /**
     * Request DTO for creating a user.
     */
    public record CreateUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone,
        UserRole role
    ) {}

    /**
     * Request DTO for updating a user.
     */
    public record UpdateUserRequest(
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        UserRole role,
        UserStatus status
    ) {}
}
