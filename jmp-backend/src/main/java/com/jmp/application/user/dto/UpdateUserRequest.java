package com.jmp.application.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * DTO for updating an existing user.
 */
public record UpdateUserRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        Set<String> roles,

        Boolean active
) {
}
