package com.jmp.application.conference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for creating a conference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConferenceRequest {

    @NotBlank
    private String name;

    private String description;

    private Instant scheduledStartAt;

    private Instant scheduledEndAt;

    private Integer maxParticipants;

    private boolean recordingEnabled;

    private boolean autoStartRecording;

    private boolean chatEnabled;

    private boolean screenSharingEnabled;

    private boolean lobbyEnabled;

    private String password;

    @NotNull
    private String tenantId;
}
