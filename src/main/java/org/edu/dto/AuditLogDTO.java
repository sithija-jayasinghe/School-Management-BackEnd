package org.edu.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;
    private Long actorUserId;
    private String actorName;
    private String actorEmail;
    private AuditAction action;
    private AuditEntityType entityType;
    private Long entityId;
    private String entityName;
    private String description;
    private LocalDateTime createdAt;
}
