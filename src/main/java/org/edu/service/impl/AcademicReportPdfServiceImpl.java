package org.edu.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.edu.dto.DocumentFileResponse;
import org.edu.entity.AcademicReport;
import org.edu.entity.AcademicReportSubject;
import org.edu.exception.DocumentStorageException;
import org.edu.service.AcademicReportPdfService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class AcademicReportPdfServiceImpl implements AcademicReportPdfService {

    private static final float LEFT = 48;
    private static final float PAGE_TOP = 790;

    @Override
    public DocumentFileResponse generate(AcademicReport report) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = PAGE_TOP;
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18, LEFT, y,
                        "School Academic Report Card");
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11, LEFT, y - 8,
                        "Student: " + report.getStudent().getName());
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11, LEFT, y,
                        "Class: " + report.getStudentClass().getName());
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11, LEFT, y,
                        "Academic Year: " + report.getAcademicTerm().getAcademicYear().getName());
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11, LEFT, y,
                        "Term: " + report.getAcademicTerm().getName());

                y -= 12;
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11, LEFT, y,
                        "Subject Results");
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD), 9, LEFT, y,
                        String.format("%-12s %-24s %6s %8s %6s", "Code", "Subject", "Exams", "Percent", "Grade"));

                for (AcademicReportSubject subject : report.getSubjects()) {
                    y = writeText(content, new PDType1Font(Standard14Fonts.FontName.COURIER), 9, LEFT, y,
                            String.format(
                                    "%-12s %-24s %6d %7s%% %6s",
                                    fit(subject.getSubjectCode(), 12),
                                    fit(subject.getSubjectName(), 24),
                                    subject.getExamCount(),
                                    format(subject.getPercentage()),
                                    subject.getGrade()
                            ));
                }

                y -= 10;
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11, LEFT, y,
                        "Overall: " + format(report.getOverallPercentage()) + "%  Grade: " + report.getOverallGrade());
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10, LEFT, y,
                        "Subjects passed: " + report.getPassedSubjectCount() + " / " + report.getSubjectCount());
                y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10, LEFT, y,
                        "Attendance: " + format(report.getAttendancePercentage()) + "%  Present: "
                                + report.getPresentCount() + "  Absent: " + report.getAbsentCount()
                                + "  Late: " + report.getLateCount() + "  Excused: " + report.getExcusedCount());

                y -= 10;
                y = writeWrapped(content, "Class Teacher Remarks: ", report.getClassTeacherRemarks(), y);
                writeWrapped(content, "Principal Remarks: ", report.getPrincipalRemarks(), y);
            }

            document.save(output);
            byte[] bytes = output.toByteArray();
            String fileName = "report-card-" + report.getStudent().getId() + "-"
                    + report.getAcademicTerm().getId() + ".pdf";
            return new DocumentFileResponse(
                    fileName,
                    "application/pdf",
                    bytes.length,
                    new ByteArrayResource(bytes)
            );
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to generate report card PDF", ex);
        }
    }

    private float writeText(
            PDPageContentStream content,
            PDType1Font font,
            float size,
            float x,
            float y,
            String value
    ) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(sanitize(value));
        content.endText();
        return y - size - 5;
    }

    private float writeWrapped(PDPageContentStream content, String label, String value, float y) throws IOException {
        String resolved = value == null || value.isBlank() ? "Not provided" : value;
        String text = label + resolved;
        int lineLength = 88;
        for (int start = 0; start < text.length(); start += lineLength) {
            int end = Math.min(start + lineLength, text.length());
            y = writeText(content, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10, LEFT, y,
                    text.substring(start, end));
        }
        return y - 5;
    }

    private String format(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2).toPlainString();
    }

    private String fit(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1);
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
