package com.jmp.domain.conference.entity;

import com.jmp.domain.common.BaseEntity;
import com.jmp.domain.tenant.entity.Tenant;
import com.jmp.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Conference entity representing a Jitsi video conference room.
 */
@Entity
@Table(name = "conferences")
public class Conference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConferenceStatus status = ConferenceStatus.SCHEDULED;

    @Column(name = "scheduled_start_at")
    private Instant scheduledStartAt;

    @Column(name = "scheduled_end_at")
    private Instant scheduledEndAt;

    @Column(name = "actual_start_at")
    private Instant actualStartAt;

    @Column(name = "actual_end_at")
    private Instant actualEndAt;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Column(name = "recording_enabled", nullable = false)
    private boolean recordingEnabled = false;

    @Column(name = "auto_start_recording", nullable = false)
    private boolean autoStartRecording = false;

    @Column(name = "chat_enabled", nullable = false)
    private boolean chatEnabled = true;

    @Column(name = "screen_sharing_enabled", nullable = false)
    private boolean screenSharingEnabled = true;

    @Column(name = "lobby_enabled", nullable = false)
    private boolean lobbyEnabled = false;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "join_url")
    private String joinUrl;

    @Column(name = "moderator_join_url")
    private String moderatorJoinUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Column(name = "jitsi_domain")
    private String jitsiDomain;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    public enum ConferenceStatus {
        SCHEDULED,
        ACTIVE,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    // Constructors
    public Conference() {
    }

    public Conference(String name, String roomId, Tenant tenant, User createdBy) {
        this.name = name;
        this.roomId = roomId;
        this.tenant = tenant;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConferenceStatus getStatus() {
        return status;
    }

    public void setStatus(ConferenceStatus status) {
        this.status = status;
    }

    public Instant getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(Instant scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public Instant getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(Instant scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    public Instant getActualStartAt() {
        return actualStartAt;
    }

    public void setActualStartAt(Instant actualStartAt) {
        this.actualStartAt = actualStartAt;
    }

    public Instant getActualEndAt() {
        return actualEndAt;
    }

    public void setActualEndAt(Instant actualEndAt) {
        this.actualEndAt = actualEndAt;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        this.recordingEnabled = recordingEnabled;
    }

    public boolean isAutoStartRecording() {
        return autoStartRecording;
    }

    public void setAutoStartRecording(boolean autoStartRecording) {
        this.autoStartRecording = autoStartRecording;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public boolean isScreenSharingEnabled() {
        return screenSharingEnabled;
    }

    public void setScreenSharingEnabled(boolean screenSharingEnabled) {
        this.screenSharingEnabled = screenSharingEnabled;
    }

    public boolean isLobbyEnabled() {
        return lobbyEnabled;
    }

    public void setLobbyEnabled(boolean lobbyEnabled) {
        this.lobbyEnabled = lobbyEnabled;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public String getModeratorJoinUrl() {
        return moderatorJoinUrl;
    }

    public void setModeratorJoinUrl(String moderatorJoinUrl) {
        this.moderatorJoinUrl = moderatorJoinUrl;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }

    public String getJitsiDomain() {
        return jitsiDomain;
    }

    public void setJitsiDomain(String jitsiDomain) {
        this.jitsiDomain = jitsiDomain;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Checks if the conference is currently active.
     */
    public boolean isActive() {
        return this.status == ConferenceStatus.ACTIVE;
    }

    /**
     * Starts the conference.
     */
    public void start() {
        this.status = ConferenceStatus.ACTIVE;
        this.actualStartAt = Instant.now();
    }

    /**
     * Ends the conference.
     */
    public void end() {
        this.status = ConferenceStatus.COMPLETED;
        this.actualEndAt = Instant.now();
    }

    /**
     * Cancels the conference.
     */
    public void cancel() {
        this.status = ConferenceStatus.CANCELLED;
    }

    /**
     * Increments the participant count.
     */
    public void incrementParticipants() {
        this.currentParticipants++;
    }

    /**
     * Decrements the participant count.
     */
    public void decrementParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }
}
