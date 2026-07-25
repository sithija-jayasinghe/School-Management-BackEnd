package org.edu.dto.teacherleave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.edu.util.StaffCategory;
import org.edu.util.TeacherLeaveDuration;
import org.edu.util.TeacherLeaveStatus;
import org.edu.util.TeacherLeaveType;

@Getter
@AllArgsConstructor
public class TeacherLeaveRequestDTO {
    private Long id;
    private Long teacherStaffId;
    private String teacherStaffCode;
    private String teacherName;
    private String teacherDesignation;
    private StaffCategory staffCategory;
    private boolean teachingCapable;
    private boolean requiresCoverage;
    private TeacherLeaveType leaveType;
    private TeacherLeaveDuration durationType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private TeacherLeaveStatus status;
    private Long reviewedByUserId;
    private String reviewedByName;
    private String reviewerRemarks;
    private LocalDateTime reviewedAt;
    private int affectedSessionCount;
    private int coveredSessionCount;
    private List<TeacherLeaveSessionDTO> affectedSessions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
