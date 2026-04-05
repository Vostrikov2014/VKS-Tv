package com.jmp.application.auth.service;

import com.jmp.application.auth.dto.AuthResponse;
import com.jmp.application.auth.dto.LoginRequest;
import com.jmp.application.jwt.JwtTokenProvider;
import com.jmp.domain.user.entity.User;
import com.jmp.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service handling user login, token generation, and refresh.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticates a user and generates JWT tokens.
     * 
     * @param request Login credentials
     * @return AuthResponse with access and refresh tokens
     * @throws BadCredentialsException if credentials are invalid
     * @throws IllegalStateException if account is locked
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login attempt for non-existent user: {}", request.email());
                    return new BadCredentialsException("Invalid email or password");
                });

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            long minutesLeft = java.time.Duration.between(Instant.now(), user.getLockedUntil()).toMinutes();
            log.warn("Login attempt for locked account: {}. Locked for {} more minutes", 
                     user.getEmail(), minutesLeft);
            throw new IllegalStateException("Account is locked. Try again in " + minutesLeft + " minutes");
        }

        // Check if user is active
        if (!user.isActive()) {
            log.warn("Login attempt for inactive account: {}", user.getEmail());
            throw new BadCredentialsException("Account is not active");
        }

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        // Reset failed attempts on successful login
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("User {} logged in successfully", user.getEmail());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getTenant().getId(),
                user.getRole().name(),
                null, // No specific room at login
                false
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );

        return AuthResponse.of(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getTenant().getId()
        );
    }

    /**
     * Refreshes access token using a valid refresh token.
     * 
     * @param refreshToken The refresh token
     * @return New AuthResponse with fresh tokens
     * @throws IllegalStateException if refresh token is invalid
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalStateException("Invalid refresh token");
        }

        // Verify it's a refresh token
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalStateException("Not a refresh token");
        }

        String userId = jwtTokenProvider.getUserId(refreshToken);
        String email = jwtTokenProvider.getSubject(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is not active");
        }

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                email,
                user.getId(),
                user.getTenant().getId(),
                user.getRole().name(),
                null,
                false
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email, user.getId());

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getTenant().getId()
        );
    }

    /**
     * Handles failed login attempts by incrementing counter and locking if threshold exceeded.
     */
    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();
        
        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            Instant lockUntil = Instant.now().plus(java.time.Duration.ofMinutes(LOCK_DURATION_MINUTES));
            user.lockUntil(lockUntil);
            log.warn("Account {} locked due to multiple failed login attempts", user.getEmail());
        }
        
        userRepository.save(user);
    }

    /**
     * Logs out a user (invalidates tokens on client side).
     * Since JWT is stateless, we rely on client to discard tokens.
     * For enhanced security, implement token blacklist in Redis.
     */
    @Transactional
    public void logout(String userId) {
        log.info("User {} logged out", userId);
        // Optional: Add token to blacklist in Redis
        // redisTemplate.opsForValue().set("logout:" + accessToken, "true", expirationTime);
    }

    /**
     * Validates a token and returns user information.
     */
    public User validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalStateException("Invalid token");
        }

        String userId = jwtTokenProvider.getUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
