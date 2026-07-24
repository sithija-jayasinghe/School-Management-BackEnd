package org.edu.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.AcademicReportStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "academic_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_academic_report_student_term",
                columnNames = {"student_id", "academic_term_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class AcademicReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_term_id", nullable = false)
    private AcademicTerm academicTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class studentClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_user_id", nullable = false)
    private User generatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by_user_id")
    private User publishedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademicReportStatus status = AcademicReportStatus.DRAFT;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal overallPercentage;

    @Column(nullable = false, length = 5)
    private String overallGrade;

    @Column(nullable = false)
    private int subjectCount;

    @Column(nullable = false)
    private int passedSubjectCount;

    @Column(nullable = false)
    private int failedSubjectCount;

    @Column(nullable = false)
    private long totalAttendanceRecords;

    @Column(nullable = false)
    private long presentCount;

    @Column(nullable = false)
    private long absentCount;

    @Column(nullable = false)
    private long lateCount;

    @Column(nullable = false)
    private long excusedCount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal attendancePercentage;

    @Column(length = 1000)
    private String classTeacherRemarks;

    @Column(length = 1000)
    private String principalRemarks;

    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "academicReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("subjectName ASC")
    private List<AcademicReportSubject> subjects = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void replaceSubjects(List<AcademicReportSubject> newSubjects) {
        subjects.clear();
        newSubjects.forEach(subject -> {
            subject.setAcademicReport(this);
            subjects.add(subject);
        });
    }
}
