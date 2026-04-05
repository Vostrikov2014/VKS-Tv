package com.jmp.application.service;

import com.jmp.application.dto.RecordingCreateRequest;
import com.jmp.application.dto.RecordingDto;
import com.jmp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing conference recordings.
 */
@Service
public class RecordingService {

    public Page<RecordingDto> findAll(UUID conferenceId, String status, UUID tenantId, User currentUser, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public RecordingDto findById(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public RecordingDto startRecording(RecordingCreateRequest request, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public RecordingDto stopRecording(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getPresignedDownloadUrl(UUID id, Long expirationSeconds, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteRecording(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String generateShareLink(UUID id, Long expirationSeconds, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
