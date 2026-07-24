package org.edu.repository;

import java.time.LocalDateTime;
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

    @Query("""
        select a from AuditLog a
        where (:keyword is null or :keyword = ''
                or lower(coalesce(a.actorName, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.actorEmail, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.entityName, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.description, '')) like lower(concat('%', :keyword, '%')))
          and (:action is null or a.action = :action)
          and (:entityType is null or a.entityType = :entityType)
          and (:actorUserId is null or a.actorUser.id = :actorUserId)
          and (:from is null or a.createdAt >= :from)
          and (:to is null or a.createdAt <= :to)
        order by a.createdAt desc
    """)
    Page<AuditLog> filterAuditLogs(
            @Param("keyword") String keyword,
            @Param("action") AuditAction action,
            @Param("entityType") AuditEntityType entityType,
            @Param("actorUserId") Long actorUserId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
