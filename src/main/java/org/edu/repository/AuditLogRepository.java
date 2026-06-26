package org.edu.repository;

import org.edu.entity.AuditLog;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(AuditEntityType entityType, Pageable pageable);

    Page<AuditLog> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId, Pageable pageable);

    @Query("""
        select a from AuditLog a
        where lower(coalesce(a.actorName, '')) like lower(concat('%', :keyword, '%'))
           or lower(coalesce(a.actorEmail, '')) like lower(concat('%', :keyword, '%'))
           or lower(coalesce(a.entityName, '')) like lower(concat('%', :keyword, '%'))
           or lower(coalesce(a.description, '')) like lower(concat('%', :keyword, '%'))
        order by a.createdAt desc
    """)
    Page<AuditLog> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
