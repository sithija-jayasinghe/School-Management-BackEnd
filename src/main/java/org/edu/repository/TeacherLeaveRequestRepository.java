package org.edu.repository;

import java.time.LocalDate;
import java.util.Collection;
import org.edu.entity.TeacherLeaveRequest;
import org.edu.util.TeacherLeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherLeaveRequestRepository extends JpaRepository<TeacherLeaveRequest, Long> {

    Page<TeacherLeaveRequest> findByTeacherIdOrderByCreatedAtDesc(Long teacherId, Pageable pageable);

    Page<TeacherLeaveRequest> findByTeacherIdAndStatusOrderByCreatedAtDesc(
            Long teacherId,
            TeacherLeaveStatus status,
            Pageable pageable
    );

    Page<TeacherLeaveRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<TeacherLeaveRequest> findByStatusOrderByCreatedAtDesc(TeacherLeaveStatus status, Pageable pageable);

    @Query("""
            select (count(request) > 0)
            from TeacherLeaveRequest request
            where request.teacher.id = :teacherId
              and request.status in :statuses
              and request.startDate <= :endDate
              and request.endDate >= :startDate
              and (:excludedId is null or request.id <> :excludedId)
            """)
    boolean existsOverlappingRequest(
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<TeacherLeaveStatus> statuses,
            @Param("excludedId") Long excludedId
    );

    @Query("""
            select (count(request) > 0)
            from TeacherLeaveRequest request
            where request.teacher.id = :teacherId
              and request.status = org.edu.util.TeacherLeaveStatus.APPROVED
              and request.startDate <= :date
              and request.endDate >= :date
            """)
    boolean hasApprovedLeaveOnDate(@Param("teacherId") Long teacherId, @Param("date") LocalDate date);
}
