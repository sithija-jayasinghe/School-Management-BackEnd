package org.edu.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportReadinessDTO {

    private Long studentId;
    private Long academicTermId;
    private boolean canGenerate;
    private long subjectCount;
    private long markCount;
    private long attendanceRecordCount;
    private List<AcademicReportReadinessItemDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicReportReadinessItemDTO {
        private String key;
        private String label;
        private String severity;
        private boolean ready;
        private String message;
    }
}
