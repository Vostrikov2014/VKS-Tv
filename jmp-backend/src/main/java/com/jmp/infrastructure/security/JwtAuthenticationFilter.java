package com.jmp.infrastructure.security;

import com.jmp.application.jwt.JwtTokenProvider;
import com.jmp.domain.user.entity.User;
import com.jmp.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 * 
 * Extracts the token from the Authorization header, validates it,
 * and sets the authentication context if valid.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // Only process access tokens, not refresh tokens
                String tokenType = jwtTokenProvider.getTokenType(jwt);
                if ("refresh".equals(tokenType)) {
                    log.debug("Skipping refresh token in authorization header");
                    filterChain.doFilter(request, response);
                    return;
                }

                String userId = jwtTokenProvider.getUserId(jwt);
                
                Optional<User> userOpt = userRepository.findById(userId);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Check if user is active
                    if (!user.isActive()) {
                        log.warn("User {} is not active", userId);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    var authorities = new ArrayList<SimpleGrantedAuthority>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    
                    // Add tenant-specific authority
                    if (user.getTenant() != null) {
                        authorities.add(new SimpleGrantedAuthority("TENANT_" + user.getTenant().getId()));
                    }

                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                authorities);

                    // Store additional claims in details for access in controllers
                    authentication.setDetails(jwt);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication for user: {}", user.getEmail());
                } else {
                    log.debug("User not found for token: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        // Also check for token in query parameter (for WebSocket connections)
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        return null;
    }
}
