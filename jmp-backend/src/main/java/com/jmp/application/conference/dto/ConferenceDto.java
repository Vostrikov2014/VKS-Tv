package com.jmp.application.conference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for conference data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenceDto {

    private String id;
    private String name;
    private String roomId;
    private String description;
    private String status;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private Instant actualStartAt;
    private Instant actualEndAt;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private boolean recordingEnabled;
    private boolean chatEnabled;
    private boolean screenSharingEnabled;
    private boolean lobbyEnabled;
    private String joinUrl;
    private String moderatorJoinUrl;
    private String tenantId;
    private String createdById;
    private String moderatorId;
    private String jitsiDomain;
    private Instant createdAt;
    private Instant updatedAt;
}
