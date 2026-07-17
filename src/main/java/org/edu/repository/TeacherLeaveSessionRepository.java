package org.edu.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.edu.entity.TeacherLeaveSession;
import org.edu.util.TeacherLeaveCoverageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherLeaveSessionRepository extends JpaRepository<TeacherLeaveSession, Long> {

    List<TeacherLeaveSession> findByLeaveRequestIdOrderBySessionDateAscStartTimeAsc(Long leaveRequestId);

    long countByLeaveRequestIdAndCoverageStatus(Long leaveRequestId, TeacherLeaveCoverageStatus coverageStatus);

    @Query("""
            select (count(session) > 0)
            from TeacherLeaveSession session
            where session.substituteTeacher.id = :staffId
              and session.coverageStatus = org.edu.util.TeacherLeaveCoverageStatus.ASSIGNED
              and session.sessionDate = :sessionDate
              and session.startTime < :endTime
              and session.endTime > :startTime
              and session.id <> :sessionId
            """)
    boolean hasSubstituteCoverageConflict(
            @Param("staffId") Long staffId,
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("sessionId") Long sessionId
    );
}
