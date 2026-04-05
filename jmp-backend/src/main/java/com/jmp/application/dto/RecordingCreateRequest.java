package com.jmp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for starting a recording.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingCreateRequest {

    @NotNull
    private UUID conferenceId;

    @NotBlank
    private String title;

    private String description;

    private String recordingType;
}
