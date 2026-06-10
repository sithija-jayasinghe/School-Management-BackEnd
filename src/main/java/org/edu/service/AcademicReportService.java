package org.edu.service;

import org.edu.dto.AcademicReportDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.AcademicReportGenerateRequest;
import org.edu.dto.request.AcademicReportUpdateRequest;
import org.edu.util.AcademicReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AcademicReportService {

    AcademicReportDTO generateReport(Long authenticatedUserId, AcademicReportGenerateRequest request);

    AcademicReportDTO regenerateReport(Long authenticatedUserId, Long reportId);

    AcademicReportDTO updateReport(Long authenticatedUserId, Long reportId, AcademicReportUpdateRequest request);

    AcademicReportDTO publishReport(Long authenticatedUserId, Long reportId);

    void deleteDraftReport(Long authenticatedUserId, Long reportId);

    AcademicReportDTO getReport(Long authenticatedUserId, Long reportId);

    Page<AcademicReportDTO> getStudentReports(Long authenticatedUserId, Long studentId, Pageable pageable);

    Page<AcademicReportDTO> getClassReports(
            Long authenticatedUserId,
            Long classId,
            Long academicTermId,
            Pageable pageable
    );

    Page<AcademicReportDTO> getPublishedStudentReports(Long studentId, Pageable pageable);

    AcademicReportDTO getPublishedReport(Long reportId);

    DocumentFileResponse downloadReportCard(Long authenticatedUserId, Long reportId);

    DocumentFileResponse downloadPublishedReportCard(Long reportId);
}
