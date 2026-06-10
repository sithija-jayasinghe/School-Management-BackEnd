package org.edu.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.AcademicReportStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private Long academicYearId;
    private String academicYearName;
    private Long academicTermId;
    private String academicTermName;
    private LocalDate termStartDate;
    private LocalDate termEndDate;
    private AcademicReportStatus status;
    private BigDecimal overallPercentage;
    private String overallGrade;
    private int subjectCount;
    private int passedSubjectCount;
    private int failedSubjectCount;
    private long totalAttendanceRecords;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long excusedCount;
    private BigDecimal attendancePercentage;
    private String classTeacherRemarks;
    private String principalRemarks;
    private Long generatedByUserId;
    private String generatedByUserName;
    private Long publishedByUserId;
    private String publishedByUserName;
    private LocalDateTime publishedAt;
    private List<AcademicReportSubjectDTO> subjects;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
