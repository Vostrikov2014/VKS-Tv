package com.jmp.presentation.rest;

import com.jmp.application.auth.dto.AuthResponse;
import com.jmp.application.auth.dto.LoginRequest;
import com.jmp.application.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and token management APIs")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns JWT tokens.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return access/refresh tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes access token using refresh token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Generate new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String refreshTokenHeader) {
        
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");
        log.debug("Token refresh requested");
        
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the current user.
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout", description = "Invalidate user session")
    public ResponseEntity<Void> logout() {
        // In stateless JWT, logout is client-side token deletion
        // Could implement token blacklist in Redis for enhanced security
        log.info("User logged out");
        return ResponseEntity.ok().build();
    }

    /**
     * Returns current user information from token.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Current User", description = "Returns authenticated user details")
    public ResponseEntity<Object> getCurrentUser() {
        // Implementation would extract user from SecurityContext
        // Returning placeholder for now
        return ResponseEntity.ok().build();
    }
}
