package com.jmp.presentation.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.jmp.application.conference.service.ConferenceService;
import com.jmp.infrastructure.jitsi.JitsiProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

/**
 * Webhook controller for receiving Jitsi events.
 * Handles conference lifecycle events from Jitsi Meet.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Jitsi webhook event handlers")
public class JitsiWebhookController {

    private final ConferenceService conferenceService;
    private final JitsiProperties jitsiProperties;

    /**
     * Handles incoming Jitsi webhook events.
     * Supports events: conference_created, conference_ended, participant_joined, participant_left, recording_status_changed
     */
    @PostMapping("/jitsi")
    @Operation(summary = "Receive Jitsi webhook", description = "Processes webhook events from Jitsi")
    public ResponseEntity<Void> handleJitsiWebhook(
            @RequestBody JitsiWebhookEvent event,
            @RequestHeader(value = "X-Jitsi-Signature", required = false) String signature,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp) {

        log.info("Received Jitsi webhook event: {} for room: {}", event.event(), event.room());

        // Verify webhook signature if enabled
        if (jitsiProperties.isVerifyWebhookSignature() && signature != null) {
            if (!verifyWebhookSignature(event, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Process event based on type
        try {
            switch (event.event()) {
                case "conference_created" -> handleConferenceCreated(event);
                case "conference_ended" -> handleConferenceEnded(event);
                case "participant_joined" -> handleParticipantJoined(event);
                case "participant_left" -> handleParticipantLeft(event);
                case "recording_status_changed" -> handleRecordingStatusChanged(event);
                default -> log.warn("Unknown event type: {}", event.event());
            }
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", event.event(), e);
            // Return 200 to prevent Jitsi from retrying (we logged the error)
            return ResponseEntity.ok().build();
        }
    }

    private void handleConferenceCreated(JitsiWebhookEvent event) {
        log.info("Conference created: {}", event.room());
        // Update conference status to ACTIVE if it exists in our database
        conferenceService.findByRoomId(event.room())
                .ifPresent(conferenceService::markAsActive);
    }

    private void handleConferenceEnded(JitsiWebhookEvent event) {
        log.info("Conference ended: {}", event.room());
        // Update conference status to COMPLETED
        conferenceService.findByRoomId(event.room())
                .ifPresent(conferenceService::markAsCompleted);
    }

    private void handleParticipantJoined(JitsiWebhookEvent event) {
        log.info("Participant joined: {} in room {}", 
                event.participant() != null ? event.participant().get("id") : "unknown", 
                event.room());
        
        // Increment participant count
        conferenceService.findByRoomId(event.room())
                .ifPresent(c -> conferenceService.incrementParticipants(c.getId()));
    }

    private void handleParticipantLeft(JitsiWebhookEvent event) {
        log.info("Participant left: {} from room {}", 
                event.participant() != null ? event.participant().get("id") : "unknown", 
                event.room());
        
        // Decrement participant count
        conferenceService.findByRoomId(event.room())
                .ifPresent(c -> conferenceService.decrementParticipants(c.getId()));
    }

    private void handleRecordingStatusChanged(JitsiWebhookEvent event) {
        log.info("Recording status changed for room: {}", event.room());
        // Handle recording status changes (started, stopped, failed)
        // This would typically trigger recording management logic
    }

    /**
     * Verifies the HMAC signature of the webhook payload.
     */
    private boolean verifyWebhookSignature(JitsiWebhookEvent event, String signature) {
        // Implementation depends on how Jitsi signs webhooks
        // Typically HMAC-SHA256 with a shared secret
        // This is a simplified example
        return true; // TODO: Implement proper signature verification
    }

    /**
     * Record representing a Jitsi webhook event.
     */
    public record JitsiWebhookEvent(
            @NotBlank String event,
            @NotBlank String room,
            Map<String, Object> participant,
            Instant timestamp,
            String sessionId,
            String recorderId,
            Boolean status
    ) {
        // Canonical constructor for validation
        public JitsiWebhookEvent {
            if (event == null || event.isBlank()) {
                throw new IllegalArgumentException("Event type is required");
            }
            if (room == null || room.isBlank()) {
                throw new IllegalArgumentException("Room ID is required");
            }
        }
    }
}
