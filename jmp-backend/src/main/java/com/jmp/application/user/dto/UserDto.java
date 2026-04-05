package com.jmp.application.user.dto;

import java.time.Instant;

/**
 * DTO for user information.
 */
public record UserDto(
    String id,
    String email,
    String firstName,
    String lastName,
    String roles,
    String status,
    boolean emailVerified,
    Instant createdAt,
    Instant updatedAt
) {
}
