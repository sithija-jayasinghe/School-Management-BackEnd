package org.edu.repository;

import org.edu.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    // Useful for showing a Class's schedule
    List<Timetable> findByStudentClassIdOrderByDayOfWeekAscStartTimeAsc(Long classId);

    @Query("""
            select t
            from Timetable t
            join fetch t.studentClass studentClass
            join fetch t.subject subject
            join fetch t.staff staff
            where studentClass.id = :classId
            order by t.dayOfWeek asc, t.startTime asc
            """)
    List<Timetable> findPortalTimetableByClassId(@Param("classId") Long classId);

    // Useful for showing a Teacher's schedule
    List<Timetable> findByStaffIdOrderByDayOfWeekAscStartTimeAsc(Long staffId);

    @Query("""
            select t
            from Timetable t
            join fetch t.studentClass studentClass
            join fetch t.subject subject
            join fetch t.staff staff
            where staff.id = :staffId
            order by t.dayOfWeek asc, t.startTime asc
            """)
    List<Timetable> findTeacherPortalScheduleByStaffId(@Param("staffId") Long staffId);

    // Conflict Check #1: Is the teacher already teaching somewhere else at this time?
    @Query("SELECT t FROM Timetable t WHERE t.staff.id = :staffId AND t.dayOfWeek = :dayOfWeek " +
           "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    List<Timetable> findTeacherConflicts(@Param("staffId") Long staffId,
                                         @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);

    // Conflict Check #2: Does the class already have a subject scheduled at this time?
    @Query("SELECT t FROM Timetable t WHERE t.studentClass.id = :classId AND t.dayOfWeek = :dayOfWeek " +
           "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    List<Timetable> findClassConflicts(@Param("classId") Long classId,
                                       @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
}
