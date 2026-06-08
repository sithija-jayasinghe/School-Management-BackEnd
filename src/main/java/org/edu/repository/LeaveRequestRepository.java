package org.edu.repository;

import java.time.LocalDate;
import java.util.Collection;
import org.edu.entity.LeaveRequest;
import org.edu.util.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveRequestStatus status, Pageable pageable);

    Page<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    Page<LeaveRequest> findByParentIdOrderByCreatedAtDesc(Long parentId, Pageable pageable);

    @Query("""
            select lr
            from LeaveRequest lr
            where exists (
                select 1
                from Class c
                where c.id = lr.student.currentClass.id
                  and c.active = true
                  and c.classTeacher.id = :staffId
            )
            or exists (
                select 1
                from Timetable t
                where t.staff.id = :staffId
                  and t.studentClass.id = lr.student.currentClass.id
            )
            order by lr.createdAt desc
            """)
    Page<LeaveRequest> findTeacherPortalLeaveRequestsByStaffId(
            @Param("staffId") Long staffId,
            Pageable pageable
    );

    @Query("""
            select lr
            from LeaveRequest lr
            where lr.status = :status
              and (
                  exists (
                      select 1
                      from Class c
                      where c.id = lr.student.currentClass.id
                        and c.active = true
                        and c.classTeacher.id = :staffId
                  )
                  or exists (
                      select 1
                      from Timetable t
                      where t.staff.id = :staffId
                        and t.studentClass.id = lr.student.currentClass.id
                  )
              )
            order by lr.createdAt desc
            """)
    Page<LeaveRequest> findTeacherPortalLeaveRequestsByStaffIdAndStatus(
            @Param("staffId") Long staffId,
            @Param("status") LeaveRequestStatus status,
            Pageable pageable
    );

    @Query("""
            select (count(lr) > 0)
            from LeaveRequest lr
            where lr.student.id = :studentId
              and lr.status in :statuses
              and lr.startDate <= :endDate
              and lr.endDate >= :startDate
            """)
    boolean existsOverlappingRequest(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<LeaveRequestStatus> statuses
    );

    @Query("""
            select (count(lr) > 0)
            from LeaveRequest lr
            where lr.student.id = :studentId
              and lr.status in :statuses
              and lr.startDate <= :endDate
              and lr.endDate >= :startDate
              and lr.id <> :id
            """)
    boolean existsOverlappingRequestExcludingId(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<LeaveRequestStatus> statuses,
            @Param("id") Long id
    );
}
