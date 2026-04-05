package com.jmp.application.dto;

import com.jmp.domain.conference.entity.Conference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for conference status updates via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenceStatusDto {

    private String id;
    private String name;
    private String roomId;
    private String status;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private Instant startedAt;
    private Instant endedAt;
    private boolean recordingEnabled;
    private boolean active;

    /**
     * Creates a ConferenceStatusDto from a Conference entity.
     */
    public static ConferenceStatusDto from(Conference conference) {
        if (conference == null) {
            return null;
        }
        return ConferenceStatusDto.builder()
                .id(conference.getId())
                .name(conference.getName())
                .roomId(conference.getRoomId())
                .status(conference.getStatus() != null ? conference.getStatus().name() : null)
                .currentParticipants(conference.getCurrentParticipants())
                .maxParticipants(conference.getMaxParticipants())
                .startedAt(conference.getActualStartAt())
                .endedAt(conference.getActualEndAt())
                .recordingEnabled(conference.isRecordingEnabled())
                .active(conference.isActive())
                .build();
    }
}
