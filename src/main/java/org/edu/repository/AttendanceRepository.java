package org.edu.repository;

import java.time.LocalDate;
import java.util.List;
import org.edu.entity.Attendance;
import org.edu.util.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Page<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId, Pageable pageable);

    Page<Attendance> findByStudentClassIdAndAttendanceDateOrderByStudentNameAsc(
            Long classId,
            LocalDate attendanceDate,
            Pageable pageable
    );

    @Modifying
    @Query("update Attendance attendance set attendance.timetable = null where attendance.timetable.id = :timetableId")
    void detachTimetable(@Param("timetableId") Long timetableId);

    @Query("""
            select attendance
            from Attendance attendance
            where (:classId is null or attendance.studentClass.id = :classId)
              and (:studentId is null or attendance.student.id = :studentId)
              and (:subjectId is null or attendance.subject.id = :subjectId)
              and (:markedByStaffId is null or attendance.markedBy.id = :markedByStaffId)
              and (:status is null or attendance.status = :status)
              and (:fromDate is null or attendance.attendanceDate >= :fromDate)
              and (:toDate is null or attendance.attendanceDate <= :toDate)
            """)
    Page<Attendance> filterAttendance(
            @Param("classId") Long classId,
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId,
            @Param("markedByStaffId") Long markedByStaffId,
            @Param("status") AttendanceStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    List<Attendance> findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate
    );

    boolean existsByStudentIdAndAttendanceDateAndTimetableId(Long studentId, LocalDate attendanceDate, Long timetableId);

    boolean existsByStudentIdAndAttendanceDateAndTimetableIdAndIdNot(
            Long studentId,
            LocalDate attendanceDate,
            Long timetableId,
            Long id
    );

    boolean existsByStudentIdAndAttendanceDateAndTimetableIsNull(Long studentId, LocalDate attendanceDate);

    boolean existsByStudentIdAndAttendanceDateAndTimetableIsNullAndIdNot(
            Long studentId,
            LocalDate attendanceDate,
            Long id
    );

    @Query("""
            select a
            from Attendance a
            join fetch a.student student
            join fetch a.studentClass studentClass
            left join fetch a.subject subject
            left join fetch a.markedBy markedBy
            where student.id = :studentId
              and a.attendanceDate between :fromDate and :toDate
            order by a.attendanceDate desc, a.id desc
            """)
    List<Attendance> findPortalAttendanceByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
