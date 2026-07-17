package org.edu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.TeacherLeaveCoverageStatus;

@Entity
@Table(
        name = "teacher_leave_sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_teacher_leave_session_date_timetable",
                columnNames = {"leave_request_id", "session_date", "timetable_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class TeacherLeaveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private TeacherLeaveRequest leaveRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "class_id_snapshot", nullable = false)
    private Long classIdSnapshot;

    @Column(name = "class_name_snapshot", nullable = false, length = 150)
    private String classNameSnapshot;

    @Column(name = "subject_id_snapshot", nullable = false)
    private Long subjectIdSnapshot;

    @Column(name = "subject_name_snapshot", nullable = false, length = 150)
    private String subjectNameSnapshot;

    @Column(name = "room_number_snapshot", length = 100)
    private String roomNumberSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_staff_id")
    private Staff substituteTeacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_status", nullable = false, length = 20)
    private TeacherLeaveCoverageStatus coverageStatus = TeacherLeaveCoverageStatus.UNASSIGNED;

    @Column(name = "coverage_remarks", length = 500)
    private String coverageRemarks;
}
