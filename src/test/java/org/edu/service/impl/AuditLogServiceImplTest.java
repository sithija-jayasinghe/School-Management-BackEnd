package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.edu.dto.AuditLogDTO;
import org.edu.entity.AuditLog;
import org.edu.entity.User;
import org.edu.repository.AuditLogRepository;
import org.edu.security.UserPrincipal;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.edu.util.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLogAuditEntryWithCurrentUser() {
        User user = new User();
        user.setId(10L);
        user.setName("Kasun Senanayake");
        user.setEmail("kasun@sms.lk");
        user.setRole(Role.ADMIN);
        user.setActive(true);

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        auditLogService.log(AuditAction.CREATE, AuditEntityType.USER, 5L, "Alice Admin", "Created admin account");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldMapAuditLogDetails() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setAction(AuditAction.UPDATE);
        auditLog.setEntityType(AuditEntityType.NOTICE);
        auditLog.setEntityId(9L);
        auditLog.setEntityName("Parent Meeting");
        auditLog.setDescription("Updated notice");
        auditLog.setActorName("Kasun Senanayake");
        auditLog.setActorEmail("kasun@sms.lk");
        auditLog.setCreatedAt(LocalDateTime.of(2026, 6, 26, 10, 30));

        when(auditLogRepository.findAll(PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(auditLog), PageRequest.of(0, 10), 1));

        AuditLogDTO response = auditLogService.getAllAuditLogs(PageRequest.of(0, 10)).getContent().getFirst();

        assertEquals(AuditAction.UPDATE, response.getAction());
        assertEquals(AuditEntityType.NOTICE, response.getEntityType());
        assertEquals("Parent Meeting", response.getEntityName());
        assertEquals("Kasun Senanayake", response.getActorName());
        assertNull(response.getActorUserId());
    }
}
