package com.jmp.application.conference.service;

import com.jmp.application.conference.dto.ConferenceDto;
import com.jmp.application.conference.dto.CreateConferenceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing conferences.
 */
@Service
public class ConferenceService {

    public Page<ConferenceDto> findAll(String tenantId, String status, Boolean active, String search, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ConferenceDto findById(String conferenceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Optional<ConferenceDto> findByRoomId(String roomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ConferenceDto create(CreateConferenceRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ConferenceDto update(String conferenceId, CreateConferenceRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void delete(String conferenceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ConferenceDto start(String conferenceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ConferenceDto end(String conferenceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String generateJoinToken(String conferenceId, boolean moderator) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getJoinUrl(String conferenceId, boolean moderator) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAsActive(ConferenceDto conference) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAsCompleted(ConferenceDto conference) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void incrementParticipants(String conferenceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void decrementParticipants(String conferenceId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
