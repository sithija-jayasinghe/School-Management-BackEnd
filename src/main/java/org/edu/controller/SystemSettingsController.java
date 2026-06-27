package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.SystemSettingsDTO;
import org.edu.dto.request.SystemSettingsUpdateRequest;
import org.edu.service.AuditLogService;
import org.edu.service.SystemSettingsService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-settings")
@RequiredArgsConstructor
@Tag(name = "System Settings", description = "School-wide configuration for administrative operations")
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get school system settings", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SystemSettingsDTO> getSystemSettings() {
        return ResponseEntity.ok(systemSettingsService.getSystemSettings());
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update school system settings", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SystemSettingsDTO> updateSystemSettings(
            @Valid @RequestBody SystemSettingsUpdateRequest request
    ) {
        SystemSettingsDTO response = systemSettingsService.updateSystemSettings(request);
        auditLogService.log(
                AuditAction.UPDATE,
                AuditEntityType.SYSTEM_SETTINGS,
                response.getId(),
                response.getSchoolName() == null ? "System Settings" : response.getSchoolName(),
                "Updated school system settings"
        );
        return ResponseEntity.ok(response);
    }
}
