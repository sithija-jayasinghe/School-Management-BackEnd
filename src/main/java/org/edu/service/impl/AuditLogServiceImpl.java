package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.AuditLogDTO;
import org.edu.entity.AuditLog;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.AuditLogRepository;
import org.edu.security.UserPrincipal;
import org.edu.service.AuditLogService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(AuditAction action, AuditEntityType entityType, Long entityId, String entityName, String description) {
        AuditLog auditLog = new AuditLog();
        User actor = resolveCurrentUser();

        auditLog.setActorUser(actor);
        auditLog.setActorName(actor == null ? "System" : actor.getName());
        auditLog.setActorEmail(actor == null ? null : actor.getEmail());
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setEntityName(entityName);
        auditLog.setDescription(description);

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> searchAuditLogs(String keyword, Pageable pageable) {
        return auditLogRepository.searchByKeyword(keyword.trim(), pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogsByEntityType(AuditEntityType entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogsByActorUserId(Long actorUserId, Pageable pageable) {
        return auditLogRepository.findByActorUserIdOrderByCreatedAtDesc(actorUserId, pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO getAuditLogById(Long auditLogId) {
        return auditLogRepository.findById(auditLogId)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + auditLogId));
    }

    private User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            return null;
        }
        return userPrincipal.getUser();
    }

    private AuditLogDTO toDto(AuditLog auditLog) {
        return new AuditLogDTO(
            auditLog.getId(),
            auditLog.getActorUser() == null ? null : auditLog.getActorUser().getId(),
            auditLog.getActorName(),
            auditLog.getActorEmail(),
            auditLog.getAction(),
            auditLog.getEntityType(),
            auditLog.getEntityId(),
            auditLog.getEntityName(),
            auditLog.getDescription(),
            auditLog.getCreatedAt()
        );
    }
}
