package com.jmp.infrastructure.scheduler;

import com.jmp.domain.repository.ConferenceRepository;
import com.jmp.domain.model.Conference;
import com.jmp.domain.model.ConferenceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Scheduler for automated conference management tasks.
 * Implements scheduled room creation, cleanup, and status synchronization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConferenceScheduler {

    private final ConferenceRepository conferenceRepository;

    /**
     * Create scheduled conferences that should start within the next 5 minutes.
     * Runs every minute to check upcoming scheduled rooms.
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    @Transactional
    public void createScheduledConferences() {
        Instant now = Instant.now();
        Instant windowEnd = now.plusSeconds(300); // 5 minutes ahead
        
        log.debug("Checking for scheduled conferences between {} and {}", now, windowEnd);
        
        List<Conference> scheduledConferences = conferenceRepository
            .findByStatusAndScheduledStartAtBetween(
                ConferenceStatus.SCHEDULED,
                now,
                windowEnd
            );
        
        for (Conference conference : scheduledConferences) {
            try {
                activateConference(conference);
            } catch (Exception e) {
                log.error("Failed to activate scheduled conference: {}", conference.getId(), e);
            }
        }
        
        if (!scheduledConferences.isEmpty()) {
            log.info("Activated {} scheduled conferences", scheduledConferences.size());
        }
    }

    /**
     * Clean up expired conferences (ended more than 24 hours ago).
     * Updates status and triggers archival processes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupExpiredConferences() {
        Instant expirationThreshold = Instant.now().minusSeconds(86400); // 24 hours ago
        
        log.debug("Cleaning up conferences ended before {}", expirationThreshold);
        
        List<Conference> expiredConferences = conferenceRepository
            .findByStatusAndEndedAtBefore(ConferenceStatus.ENDED, expirationThreshold);
        
        for (Conference conference : expiredConferences) {
            try {
                conference.setStatus(ConferenceStatus.ARCHIVED);
                conference.setArchivedAt(Instant.now());
                conferenceRepository.save(conference);
                
                log.info("Archived expired conference: {}", conference.getId());
            } catch (Exception e) {
                log.error("Failed to archive conference: {}", conference.getId(), e);
            }
        }
        
        if (!expiredConferences.isEmpty()) {
            log.info("Archived {} expired conferences", expiredConferences.size());
        }
    }

    /**
     * Synchronize conference status with Jitsi state.
     * Detects orphaned active conferences and updates their status.
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    @Transactional
    public void synchronizeConferenceStatus() {
        log.debug("Synchronizing conference status with Jitsi");
        
        List<Conference> activeConferences = conferenceRepository
            .findByStatus(ConferenceStatus.ACTIVE);
        
        int synchronizedCount = 0;
        for (Conference conference : activeConferences) {
            try {
                // Check if conference still exists in Jitsi
                boolean stillActive = checkConferenceInJitsi(conference.getRoomName());
                
                if (!stillActive && conference.getStartedAt() != null) {
                    Instant now = Instant.now();
                    // If conference ended more than 10 minutes ago according to Jitsi
                    if (now.isAfter(conference.getStartedAt().plusSeconds(600))) {
                        conference.setStatus(ConferenceStatus.ENDED);
                        conference.setEndedAt(now);
                        conferenceRepository.save(conference);
                        synchronizedCount++;
                        
                        log.info("Synchronized conference status to ENDED: {}", conference.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to synchronize conference: {}", conference.getId(), e);
            }
        }
        
        if (synchronizedCount > 0) {
            log.info("Synchronized {} conference statuses", synchronizedCount);
        }
    }

    /**
     * Send reminder notifications for upcoming conferences.
     * Sends emails 15 minutes before scheduled start time.
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void sendConferenceReminders() {
        Instant now = Instant.now();
        Instant reminderWindow = now.plusSeconds(900); // 15 minutes ahead
        
        log.debug("Sending reminders for conferences starting around {}", reminderWindow);
        
        List<Conference> upcomingConferences = conferenceRepository
            .findByStatusAndScheduledStartAtBetween(
                ConferenceStatus.SCHEDULED,
                now,
                reminderWindow
            );
        
        for (Conference conference : upcomingConferences) {
            try {
                sendReminderNotification(conference);
            } catch (Exception e) {
                log.error("Failed to send reminder for conference: {}", conference.getId(), e);
            }
        }
        
        if (!upcomingConferences.isEmpty()) {
            log.info("Sent {} conference reminders", upcomingConferences.size());
        }
    }

    /**
     * Generate daily statistics report at midnight UTC.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void generateDailyStatistics() {
        ZonedDateTime yesterday = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1);
        Instant startOfDay = yesterday.toLocalDate().atStartOfDay(yesterday.getZone()).toInstant();
        Instant endOfDay = startOfDay.plusSeconds(86400);
        
        log.info("Generating daily statistics for {}", yesterday.toLocalDate());
        
        try {
            // Aggregate conference statistics
            long totalConferences = conferenceRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long activeConferences = conferenceRepository.countByStatusAndCreatedAtBetween(
                ConferenceStatus.ACTIVE, startOfDay, endOfDay);
            
            log.info("Daily statistics - Total: {}, Active: {}", totalConferences, activeConferences);
            
            // TODO: Store statistics in database and send reports
        } catch (Exception e) {
            log.error("Failed to generate daily statistics", e);
        }
    }

    private void activateConference(Conference conference) {
        conference.setStatus(ConferenceStatus.ACTIVE);
        conference.setStartedAt(Instant.now());
        conferenceRepository.save(conference);
        
        log.info("Activated scheduled conference: {} in room: {}", 
                conference.getId(), conference.getRoomName());
        
        // TODO: Trigger notification to participants
    }

    private boolean checkConferenceInJitsi(String roomName) {
        // TODO: Implement Jitsi API call to check if room is active
        // For now, return true as placeholder
        return true;
    }

    private void sendReminderNotification(Conference conference) {
        // TODO: Implement email notification via EmailService
        log.debug("Sending reminder for conference: {} to participants", conference.getId());
    }
}
