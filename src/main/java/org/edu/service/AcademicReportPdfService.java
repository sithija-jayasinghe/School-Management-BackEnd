package org.edu.service;

import org.edu.dto.DocumentFileResponse;
import org.edu.entity.AcademicReport;

public interface AcademicReportPdfService {

    DocumentFileResponse generate(AcademicReport report);
}
