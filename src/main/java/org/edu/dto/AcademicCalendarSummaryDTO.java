package org.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicCalendarSummaryDTO {

    private AcademicYearDTO currentAcademicYear;

    private AcademicTermDTO currentAcademicTerm;

    private AcademicTermDTO nextAcademicTerm;

    private long activeTermCount;

    private Long daysRemainingInCurrentTerm;

    private Integer currentTermProgressPercent;

    private List<String> warnings;
}
