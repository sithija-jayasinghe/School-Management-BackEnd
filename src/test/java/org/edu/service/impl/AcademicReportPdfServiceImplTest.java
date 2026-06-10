package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.edu.dto.DocumentFileResponse;
import org.edu.entity.AcademicReport;
import org.edu.entity.AcademicReportSubject;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Student;
import org.junit.jupiter.api.Test;

class AcademicReportPdfServiceImplTest {

    private final AcademicReportPdfServiceImpl pdfService = new AcademicReportPdfServiceImpl();

    @Test
    void shouldGeneratePdfReportCard() throws Exception {
        AcademicYear year = new AcademicYear();
        year.setName("2026");
        AcademicTerm term = new AcademicTerm();
        term.setId(40L);
        term.setName("Term 1");
        term.setAcademicYear(year);

        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setName("Grade 10A");
        Student student = new Student();
        student.setId(20L);
        student.setName("Student User");

        AcademicReportSubject subject = new AcademicReportSubject();
        subject.setSubjectCode("MATH");
        subject.setSubjectName("Mathematics");
        subject.setExamCount(2);
        subject.setPercentage(new BigDecimal("82.50"));
        subject.setGrade("A");

        AcademicReport report = new AcademicReport();
        report.setStudent(student);
        report.setStudentClass(studentClass);
        report.setAcademicTerm(term);
        report.setOverallPercentage(new BigDecimal("82.50"));
        report.setOverallGrade("A");
        report.setSubjectCount(1);
        report.setPassedSubjectCount(1);
        report.setAttendancePercentage(new BigDecimal("91.50"));
        report.setPresentCount(45);
        report.setAbsentCount(2);
        report.setLateCount(1);
        report.setExcusedCount(1);
        report.setClassTeacherRemarks("Excellent progress");
        report.setPrincipalRemarks("Well done");
        report.replaceSubjects(List.of(subject));

        DocumentFileResponse response = pdfService.generate(report);
        byte[] bytes = response.getResource().getInputStream().readAllBytes();

        assertEquals("application/pdf", response.getContentType());
        assertTrue(response.getFileName().endsWith(".pdf"));
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
