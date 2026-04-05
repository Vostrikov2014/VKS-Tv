package com.jmp.application.service;

import com.jmp.application.dto.TenantCreateRequest;
import com.jmp.application.dto.TenantDto;
import com.jmp.application.dto.TenantUpdateRequest;
import com.jmp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Service for managing tenants.
 */
@Service
public class TenantService {

    public Page<TenantDto> findAll(String status, User currentUser, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TenantDto findById(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TenantDto createTenant(TenantCreateRequest request, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TenantDto updateTenant(UUID id, TenantUpdateRequest request, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TenantDto suspendTenant(UUID id, String reason, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TenantDto activateTenant(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Object getQuotasAndUsage(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Object updateQuotas(UUID id, Map<String, Integer> quotas, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteTenant(UUID id, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Object getStatistics(UUID id, String period, User currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
