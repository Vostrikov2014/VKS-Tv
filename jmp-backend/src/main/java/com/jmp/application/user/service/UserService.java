package com.jmp.application.user.service;

import com.jmp.application.user.dto.CreateUserRequest;
import com.jmp.application.user.dto.UpdateUserRequest;
import com.jmp.application.user.dto.UserDto;
import com.jmp.domain.audit.entity.AuditLog;
import com.jmp.domain.audit.entity.AuditLog.AuditEventType;
import com.jmp.domain.audit.repository.AuditLogRepository;
import com.jmp.domain.tenant.entity.Tenant;
import com.jmp.domain.tenant.repository.TenantRepository;
import com.jmp.domain.user.entity.User;
import com.jmp.domain.user.entity.User.UserRole;
import com.jmp.domain.user.entity.User.UserStatus;
import com.jmp.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final TenantRepository tenantRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds all users with optional filtering.
     */
    public Page<UserDto> findAll(String tenantId, String role, String status, String search, Pageable pageable) {
        Page<User> users;
        
        if (StringUtils.hasText(search)) {
            users = userRepository.searchByTenantAndNameOrEmail(tenantId, search, pageable);
        } else if (StringUtils.hasText(tenantId)) {
            users = userRepository.findByTenantId(tenantId, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(this::toDto);
    }

    /**
     * Finds a user by ID.
     */
    public UserDto findById(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toDto(user);
    }

    /**
     * Creates a new user.
     */
    @Transactional
    public UserDto create(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        // Get tenant
        Tenant tenant = null;
        if (StringUtils.hasText(request.tenantId())) {
            tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + request.tenantId()));
        } else {
            // Get current user's tenant
            String currentUserId = getCurrentUserId();
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
            tenant = currentUser.getTenant();
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.password());

        // Create user entity
        User user = new User();
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(encodedPassword);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setTenant(tenant);
        user.setStatus(UserStatus.ACTIVE);
        
        // Set roles
        if (request.roles() != null && !request.roles().isEmpty()) {
            Set<UserRole> roles = new HashSet<>();
            for (String roleName : request.roles()) {
                try {
                    roles.add(UserRole.valueOf(roleName));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role: {}", roleName);
                }
            }
            if (!roles.isEmpty()) {
                user.setRoles(roles);
            }
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Audit log
        logAuditEvent(
            AuditEventType.USER_CREATED,
            "User created",
            "USER",
            savedUser.getId(),
            "Created user: " + savedUser.getEmail(),
            getCurrentUserId(),
            tenant.getId(),
            true,
            null
        );

        log.info("Created user: {}", savedUser.getEmail());
        return toDto(savedUser);
    }

    /**
     * Updates a user fully.
     */
    @Transactional
    public UserDto update(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update fields
        if (StringUtils.hasText(request.email())) {
            if (!user.getEmail().equalsIgnoreCase(request.email()) && 
                userRepository.existsByEmailIgnoreCase(request.email())) {
                throw new IllegalArgumentException("Email already exists: " + request.email());
            }
            user.setEmail(request.email().toLowerCase().trim());
        }
        
        if (StringUtils.hasText(request.password())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        
        if (StringUtils.hasText(request.firstName())) {
            user.setFirstName(request.firstName());
        }
        
        if (StringUtils.hasText(request.lastName())) {
            user.setLastName(request.lastName());
        }

        // Update roles
        if (request.roles() != null) {
            Set<UserRole> roles = new HashSet<>();
            for (String roleName : request.roles()) {
                try {
                    roles.add(UserRole.valueOf(roleName));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role: {}", roleName);
                }
            }
            user.setRoles(roles);
        }

        // Update status
        if (request.active() != null) {
            user.setStatus(request.active() ? UserStatus.ACTIVE : UserStatus.SUSPENDED);
        }

        User updatedUser = userRepository.save(user);
        
        log.info("Updated user: {}", updatedUser.getEmail());
        return toDto(updatedUser);
    }

    /**
     * Partially updates a user.
     */
    @Transactional
    public UserDto partialUpdate(String userId, UpdateUserRequest request) {
        return update(userId, request);
    }

    /**
     * Deletes a user (soft delete).
     */
    @Transactional
    public void delete(String userId) {
        User user = userRepository.findById(userId)
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
            getCurrentUserId(),
            user.getTenant().getId(),
            true,
            null
        );

        log.info("Deleted user: {}", user.getEmail());
    }

    /**
     * Activates a user.
     */
    @Transactional
    public UserDto activate(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());

        User activatedUser = userRepository.save(user);

        logAuditEvent(
            AuditEventType.USER_ACTIVATED,
            "User activated",
            "USER",
            userId,
            "User activated: " + activatedUser.getEmail(),
            getCurrentUserId(),
            user.getTenant().getId(),
            true,
            null
        );

        log.info("Activated user: {}", activatedUser.getEmail());
        return toDto(activatedUser);
    }

    /**
     * Deactivates a user.
     */
    @Transactional
    public UserDto deactivate(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.SUSPENDED);

        User deactivatedUser = userRepository.save(user);

        logAuditEvent(
            AuditEventType.USER_SUSPENDED,
            "User suspended",
            "USER",
            userId,
            "User suspended: " + deactivatedUser.getEmail(),
            getCurrentUserId(),
            user.getTenant().getId(),
            true,
            null
        );

        log.info("Deactivated user: {}", deactivatedUser.getEmail());
        return toDto(deactivatedUser);
    }

    /**
     * Assigns a role to a user.
     */
    @Transactional
    public UserDto assignRole(String userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        Set<UserRole> roles = user.getRoles();
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(userRole);
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);

        logAuditEvent(
            AuditEventType.ROLE_CHANGED,
            "Role assigned",
            "USER",
            userId,
            "Assigned role: " + role,
            getCurrentUserId(),
            user.getTenant().getId(),
            true,
            null
        );

        log.info("Assigned role {} to user: {}", role, updatedUser.getEmail());
        return toDto(updatedUser);
    }

    /**
     * Removes a role from a user.
     */
    @Transactional
    public UserDto removeRole(String userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        Set<UserRole> roles = user.getRoles();
        if (roles != null) {
            roles.remove(userRole);
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);

        logAuditEvent(
            AuditEventType.ROLE_CHANGED,
            "Role removed",
            "USER",
            userId,
            "Removed role: " + role,
            getCurrentUserId(),
            user.getTenant().getId(),
            true,
            null
        );

        log.info("Removed role {} from user: {}", role, updatedUser.getEmail());
        return toDto(updatedUser);
    }

    /**
     * Checks if the current user is the specified user.
     */
    public boolean isCurrentUser(String userId) {
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * Checks if the current user is a tenant admin of the specified user.
     */
    public boolean isTenantAdminOfUser(String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) {
            return false;
        }

        // Check if current user is tenant admin
        if (!currentUser.getRoles().contains(UserRole.TENANT_ADMIN)) {
            return false;
        }

        // Check if both users belong to the same tenant
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return false;
        }

        return currentUser.getTenant().getId().equals(targetUser.getTenant().getId());
    }

    /**
     * Gets the current authenticated user ID.
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Converts User entity to DTO.
     */
    private UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles() != null ? user.getRoles().toString() : "",
            user.getStatus().name(),
            user.isEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
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
        try {
            AuditLog auditLog = new AuditLog(eventType, action, description);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setOccurredAt(Instant.now());

            if (userId != null) {
                userRepository.findById(userId).ifPresent(auditLog::setUser);
            }

            auditLog.sanitize();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log entry", e);
        }
    }
}
