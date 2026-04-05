package com.jmp.presentation.rest;

import com.jmp.application.dto.RecordingDto;
import com.jmp.application.dto.RecordingCreateRequest;
import com.jmp.application.service.RecordingService;
import com.jmp.infrastructure.security.CurrentUser;
import com.jmp.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * REST Controller for managing conference recordings.
 * Implements RBAC access control and audit logging.
 */
@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
@Tag(name = "Recordings", description = "Conference recording management API")
public class RecordingController {

    private final RecordingService recordingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'AUDITOR')")
    @Operation(summary = "Get all recordings with pagination and filtering")
    public ResponseEntity<Page<RecordingDto>> getRecordings(
            @RequestParam(required = false) UUID conferenceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID tenantId,
            @CurrentUser User currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<RecordingDto> recordings = recordingService.findAll(
            conferenceId, status, tenantId, currentUser, pageable);
        return ResponseEntity.ok(recordings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'AUDITOR')")
    @Operation(summary = "Get recording by ID")
    public ResponseEntity<RecordingDto> getRecording(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        RecordingDto recording = recordingService.findById(id, currentUser);
        return ResponseEntity.ok(recording);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Start conference recording")
    public ResponseEntity<RecordingDto> startRecording(
            @Valid @RequestBody RecordingCreateRequest request,
            @CurrentUser User currentUser) {
        
        RecordingDto recording = recordingService.startRecording(request, currentUser);
        return ResponseEntity.created(URI.create("/api/v1/recordings/" + recording.getId()))
                .body(recording);
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Stop conference recording")
    public ResponseEntity<RecordingDto> stopRecording(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        RecordingDto recording = recordingService.stopRecording(id, currentUser);
        return ResponseEntity.ok(recording);
    }

    @GetMapping("/{id}/download-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Get presigned download URL for recording")
    public ResponseEntity<String> getDownloadUrl(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "3600") Long expirationSeconds,
            @CurrentUser User currentUser) {
        
        String url = recordingService.getPresignedDownloadUrl(id, expirationSeconds, currentUser);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete recording")
    public ResponseEntity<Void> deleteRecording(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        recordingService.deleteRecording(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Generate sharing link for recording")
    public ResponseEntity<String> shareRecording(
            @PathVariable UUID id,
            @RequestParam Long expirationSeconds,
            @CurrentUser User currentUser) {
        
        String shareUrl = recordingService.generateShareLink(id, expirationSeconds, currentUser);
        return ResponseEntity.ok(shareUrl);
    }
}
