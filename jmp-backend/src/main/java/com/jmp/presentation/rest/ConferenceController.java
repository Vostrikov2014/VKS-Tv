package com.jmp.presentation.rest;

import com.jmp.application.conference.dto.ConferenceDto;
import com.jmp.application.conference.dto.CreateConferenceRequest;
import com.jmp.application.conference.service.ConferenceService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing conferences.
 */
@RestController
@RequestMapping("/api/v1/conferences")
@RequiredArgsConstructor
@Tag(name = "Conferences", description = "Conference management API")
public class ConferenceController {

    private final ConferenceService conferenceService;

    /**
     * Get all conferences with pagination and filtering.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR')")
    @Operation(summary = "Get all conferences", description = "Returns paginated list of conferences")
    public ResponseEntity<Page<ConferenceDto>> getConferences(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        
        Page<ConferenceDto> conferences = conferenceService.findAll(tenantId, status, active, search, pageable);
        return ResponseEntity.ok(conferences);
    }

    /**
     * Get conference by ID.
     */
    @GetMapping("/{conferenceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR')")
    @Operation(summary = "Get conference by ID", description = "Returns conference details by ID")
    public ResponseEntity<ConferenceDto> getConferenceById(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId) {
        
        ConferenceDto conference = conferenceService.findById(conferenceId);
        return ResponseEntity.ok(conference);
    }

    /**
     * Create a new conference.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR')")
    @Operation(summary = "Create conference", description = "Creates a new conference room")
    public ResponseEntity<ConferenceDto> createConference(
            @Valid @RequestBody CreateConferenceRequest request) {
        
        ConferenceDto createdConference = conferenceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConference);
    }

    /**
     * Update conference.
     */
    @PutMapping("/{conferenceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR')")
    @Operation(summary = "Update conference", description = "Updates conference configuration")
    public ResponseEntity<ConferenceDto> updateConference(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId,
            @Valid @RequestBody CreateConferenceRequest request) {
        
        ConferenceDto updatedConference = conferenceService.update(conferenceId, request);
        return ResponseEntity.ok(updatedConference);
    }

    /**
     * Delete conference.
     */
    @DeleteMapping("/{conferenceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Delete conference", description = "Soft deletes a conference")
    public ResponseEntity<Void> deleteConference(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId) {
        
        conferenceService.delete(conferenceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Start conference.
     */
    @PostMapping("/{conferenceId}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR')")
    @Operation(summary = "Start conference", description = "Starts an active conference session")
    public ResponseEntity<ConferenceDto> startConference(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId) {
        
        ConferenceDto startedConference = conferenceService.start(conferenceId);
        return ResponseEntity.ok(startedConference);
    }

    /**
     * End conference.
     */
    @PostMapping("/{conferenceId}/end")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR')")
    @Operation(summary = "End conference", description = "Ends an active conference session")
    public ResponseEntity<ConferenceDto> endConference(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId) {
        
        ConferenceDto endedConference = conferenceService.end(conferenceId);
        return ResponseEntity.ok(endedConference);
    }

    /**
     * Generate JWT token for joining conference.
     */
    @PostMapping("/{conferenceId}/token")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR', 'USER')")
    @Operation(summary = "Generate join token", description = "Generates JWT token for joining conference")
    public ResponseEntity<String> generateToken(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId,
            @RequestParam(required = false, defaultValue = "false") boolean moderator) {
        
        String token = conferenceService.generateJoinToken(conferenceId, moderator);
        return ResponseEntity.ok(token);
    }

    /**
     * Get conference join URL.
     */
    @GetMapping("/{conferenceId}/join-url")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MODERATOR', 'USER')")
    @Operation(summary = "Get join URL", description = "Returns the Jitsi join URL for the conference")
    public ResponseEntity<String> getJoinUrl(
            @Parameter(description = "Conference ID") @PathVariable String conferenceId,
            @RequestParam(required = false, defaultValue = "false") boolean moderator) {
        
        String joinUrl = conferenceService.getJoinUrl(conferenceId, moderator);
        return ResponseEntity.ok(joinUrl);
    }
}
