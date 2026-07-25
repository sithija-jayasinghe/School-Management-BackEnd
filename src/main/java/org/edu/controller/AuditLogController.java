package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AuditLogDTO;
import org.edu.service.AuditLogService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "Review sensitive administrative actions across the system")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List audit log entries")
    public Page<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        return auditLogService.getAllAuditLogs(pageable);
    }

    @GetMapping("/{auditLogId}")
    @Operation(summary = "Get an audit log entry by id")
    public AuditLogDTO getAuditLogById(@PathVariable Long auditLogId) {
        return auditLogService.getAuditLogById(auditLogId);
    }

    @GetMapping("/search")
    @Operation(summary = "Search audit logs by actor, entity, or description")
    public Page<AuditLogDTO> searchAuditLogs(@RequestParam String keyword, Pageable pageable) {
        return auditLogService.searchAuditLogs(keyword, pageable);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter audit logs by keyword, action, entity, actor, and date range")
    public Page<AuditLogDTO> filterAuditLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable
    ) {
        return auditLogService.filterAuditLogs(keyword, action, entityType, actorUserId, from, to, pageable);
    }

    @GetMapping("/actions/{action}")
    @Operation(summary = "List audit logs by action")
    public Page<AuditLogDTO> getAuditLogsByAction(@PathVariable AuditAction action, Pageable pageable) {
        return auditLogService.getAuditLogsByAction(action, pageable);
    }

    @GetMapping("/entities/{entityType}")
    @Operation(summary = "List audit logs by entity type")
    public Page<AuditLogDTO> getAuditLogsByEntityType(@PathVariable AuditEntityType entityType, Pageable pageable) {
        return auditLogService.getAuditLogsByEntityType(entityType, pageable);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "List audit logs created by a user")
    public Page<AuditLogDTO> getAuditLogsByActorUserId(@PathVariable Long userId, Pageable pageable) {
        return auditLogService.getAuditLogsByActorUserId(userId, pageable);
    }
}
