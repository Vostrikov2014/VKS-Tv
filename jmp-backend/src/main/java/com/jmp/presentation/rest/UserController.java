package com.jmp.presentation.rest;

import com.jmp.application.user.dto.UserDto;
import com.jmp.application.user.dto.CreateUserRequest;
import com.jmp.application.user.dto.UpdateUserRequest;
import com.jmp.application.user.service.UserService;
import com.jmp.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing users.
 * Provides endpoints for CRUD operations on user entities.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination and filtering.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Get all users", description = "Returns paginated list of users with optional filtering")
    public ResponseEntity<Page<UserDto>> getUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        Page<UserDto> users = userService.findAll(tenantId, role, status, search, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "Get user by ID", description = "Returns user details by ID")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        UserDto user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Create a new user.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Create user", description = "Creates a new user in the system")
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        
        UserDto createdUser = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Update user fully.
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "Update user", description = "Fully updates user information")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        
        UserDto updatedUser = userService.update(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Partially update user.
     */
    @PatchMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "Partially update user", description = "Partially updates user information")
    public ResponseEntity<UserDto> patchUser(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        
        UserDto updatedUser = userService.partialUpdate(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @userService.isTenantAdminOfUser(#userId)")
    @Operation(summary = "Delete user", description = "Soft deletes a user from the system")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate user account.
     */
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Activate user", description = "Activates a user account")
    public ResponseEntity<UserDto> activateUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        UserDto activatedUser = userService.activate(userId);
        return ResponseEntity.ok(activatedUser);
    }

    /**
     * Deactivate user account.
     */
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account")
    public ResponseEntity<UserDto> deactivateUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        UserDto deactivatedUser = userService.deactivate(userId);
        return ResponseEntity.ok(deactivatedUser);
    }

    /**
     * Assign role to user.
     */
    @PostMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Assign role to user", description = "Assigns a role to a user")
    public ResponseEntity<UserDto> assignRole(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Role name") @PathVariable String role) {
        
        UserDto updatedUser = userService.assignRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Remove role from user.
     */
    @DeleteMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user")
    public ResponseEntity<UserDto> removeRole(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Role name") @PathVariable String role) {
        
        UserDto updatedUser = userService.removeRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
}
