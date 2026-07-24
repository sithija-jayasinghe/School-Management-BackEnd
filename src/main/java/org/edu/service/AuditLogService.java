package org.edu.service;

import java.time.LocalDateTime;
import org.edu.dto.AuditLogDTO;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void log(AuditAction action, AuditEntityType entityType, Long entityId, String entityName, String description);

    Page<AuditLogDTO> getAllAuditLogs(Pageable pageable);

    Page<AuditLogDTO> filterAuditLogs(
            String keyword,
            AuditAction action,
            AuditEntityType entityType,
            Long actorUserId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<AuditLogDTO> searchAuditLogs(String keyword, Pageable pageable);

    Page<AuditLogDTO> getAuditLogsByAction(AuditAction action, Pageable pageable);

    Page<AuditLogDTO> getAuditLogsByEntityType(AuditEntityType entityType, Pageable pageable);

    Page<AuditLogDTO> getAuditLogsByActorUserId(Long actorUserId, Pageable pageable);

    AuditLogDTO getAuditLogById(Long auditLogId);
}
