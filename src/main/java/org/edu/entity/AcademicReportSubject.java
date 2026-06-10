package org.edu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "academic_report_subjects")
@Getter
@Setter
@NoArgsConstructor
public class AcademicReportSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_report_id", nullable = false)
    private AcademicReport academicReport;

    @Column(nullable = false)
    private Long subjectId;

    @Column(nullable = false, length = 30)
    private String subjectCode;

    @Column(nullable = false, length = 150)
    private String subjectName;

    @Column(nullable = false)
    private int examCount;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal totalMarksObtained;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal totalMaxMarks;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(nullable = false, length = 5)
    private String grade;

    @Column(nullable = false)
    private boolean passed;
}
