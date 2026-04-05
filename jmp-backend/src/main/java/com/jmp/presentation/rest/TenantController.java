package com.jmp.presentation.rest;

import com.jmp.application.dto.TenantDto;
import com.jmp.application.dto.TenantCreateRequest;
import com.jmp.application.dto.TenantUpdateRequest;
import com.jmp.application.service.TenantService;
import com.jmp.infrastructure.security.CurrentUser;
import com.jmp.domain.user.entity.User;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * REST Controller for managing tenants (multi-tenancy support).
 * Implements strict isolation and quota management.
 */
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Multi-tenant management API")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all tenants with pagination")
    public ResponseEntity<Page<TenantDto>> getTenants(
            @RequestParam(required = false) String status,
            @CurrentUser User currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<TenantDto> tenants = tenantService.findAll(status, currentUser, pageable);
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<TenantDto> getTenant(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        TenantDto tenant = tenantService.findById(id, currentUser);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create new tenant")
    public ResponseEntity<TenantDto> createTenant(
            @Valid @RequestBody TenantCreateRequest request,
            @CurrentUser User currentUser) {
        
        TenantDto tenant = tenantService.createTenant(request, currentUser);
        return ResponseEntity.created(URI.create("/api/v1/tenants/" + tenant.getId()))
                .body(tenant);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Update tenant configuration")
    public ResponseEntity<TenantDto> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody TenantUpdateRequest request,
            @CurrentUser User currentUser) {
        
        TenantDto tenant = tenantService.updateTenant(id, request, currentUser);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Suspend tenant access")
    public ResponseEntity<TenantDto> suspendTenant(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser User currentUser) {
        
        TenantDto tenant = tenantService.suspendTenant(id, reason, currentUser);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activate suspended tenant")
    public ResponseEntity<TenantDto> activateTenant(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        TenantDto tenant = tenantService.activateTenant(id, currentUser);
        return ResponseEntity.ok(tenant);
    }

    @GetMapping("/{id}/quotas")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Get tenant quotas and usage")
    public ResponseEntity<?> getTenantQuotas(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        var quotas = tenantService.getQuotasAndUsage(id, currentUser);
        return ResponseEntity.ok(quotas);
    }

    @PutMapping("/{id}/quotas")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update tenant quotas")
    public ResponseEntity<?> updateTenantQuotas(
            @PathVariable UUID id,
            @Valid @RequestBody Map<String, Integer> quotas,
            @CurrentUser User currentUser) {
        
        var updated = tenantService.updateQuotas(id, quotas, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete tenant (soft delete)")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable UUID id,
            @CurrentUser User currentUser) {
        
        tenantService.deleteTenant(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'AUDITOR')")
    @Operation(summary = "Get tenant statistics and analytics")
    public ResponseEntity<?> getTenantStatistics(
            @PathVariable UUID id,
            @RequestParam(required = false) String period,
            @CurrentUser User currentUser) {
        
        var stats = tenantService.getStatistics(id, period, currentUser);
        return ResponseEntity.ok(stats);
    }
}
