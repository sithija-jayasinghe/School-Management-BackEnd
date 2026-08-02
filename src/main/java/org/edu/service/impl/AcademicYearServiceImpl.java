package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicCalendarSummaryDTO;
import org.edu.dto.AcademicTermDTO;
import org.edu.dto.AcademicYearDTO;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Grade;
import org.edu.exception.ResourceNotFoundException;
import org.edu.filter.AcademicYearFilterDefinitions;
import org.edu.filter.FilterSpecifications;
import org.edu.mapper.AcademicYearMapper;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.edu.service.AcademicYearService;
import org.edu.util.ClassSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermRepository academicTermRepository;
    private final GradeRepository gradeRepository;
    private final ClassRepository classRepository;
    private final AcademicYearMapper academicYearMapper;

    @Override
    public AcademicYearDTO createAcademicYear(AcademicYearDTO academicYearDTO) {
        validateDateRange(academicYearDTO.getStartDate(), academicYearDTO.getEndDate());
        validateUniqueName(academicYearDTO.getName());

        if (overlapsExistingAcademicYear(academicYearDTO.getStartDate(), academicYearDTO.getEndDate())) {
            throw new IllegalArgumentException("Academic year date range overlaps with an existing academic year");
        }

        AcademicYear academicYear = academicYearMapper.toEntity(academicYearDTO);
        academicYear.setActive(true);
        academicYear.setCurrent(false);
        AcademicYear savedAcademicYear = academicYearRepository.save(academicYear);
        ensurePrimaryClasses(savedAcademicYear);

        return academicYearMapper.toDTO(savedAcademicYear);
    }

    @Override
    public AcademicYearDTO updateAcademicYear(Long id, AcademicYearDTO academicYearDTO) {
        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        LocalDate startDate = academicYearDTO.getStartDate() != null ? academicYearDTO.getStartDate() : academicYear.getStartDate();
        LocalDate endDate = academicYearDTO.getEndDate() != null ? academicYearDTO.getEndDate() : academicYear.getEndDate();
        validateDateRange(startDate, endDate);

        if (academicYearDTO.getName() != null
                && !academicYearDTO.getName().equalsIgnoreCase(academicYear.getName())
                && academicYearRepository.existsByNameIgnoreCaseAndIdNot(academicYearDTO.getName(), id)) {
            throw new IllegalArgumentException("Academic year already exists with name: " + academicYearDTO.getName());
        }

        if (academicYearRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(endDate, startDate, id)) {
            throw new IllegalArgumentException("Academic year date range overlaps with an existing academic year");
        }

        academicYearMapper.updateEntityFromDTO(academicYearDTO, academicYear);

        return academicYearMapper.toDTO(academicYear);
    }

    @Override
    public void deleteAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        if (!academicYear.isActive()) {
            throw new IllegalStateException("Academic year already inactive");
        }

        if (academicYear.isCurrent()) {
            throw new IllegalStateException("Current academic year cannot be deactivated");
        }

        clearTermsForAcademicYear(academicYear.getId());
        academicYear.setActive(false);
    }

    @Override
    public void activateAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        academicYear.setActive(true);
    }

    @Override
    public void closeAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        academicYear.setCurrent(false);
        clearTermsForAcademicYear(academicYear.getId());
        academicYear.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicYearDTO> getAllAcademicYears(Pageable pageable) {
        return academicYearRepository.findByActiveTrue(pageable)
                .map(academicYearMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicYearDTO> filterAcademicYears(Map<String, String> filters, Pageable pageable) {
        Map<String, String> normalizedFilters = new LinkedHashMap<>(filters);
        String status = normalizedFilters.remove("status");
        Boolean active = switch (status == null ? "active" : status.trim().toLowerCase()) {
            case "all" -> null;
            case "inactive" -> false;
            default -> true;
        };

        if (active != null) {
            normalizedFilters.put("active", active.toString());
        }

        return academicYearRepository.findAll(
                        FilterSpecifications.build(normalizedFilters, AcademicYearFilterDefinitions.definitions()),
                        pageable
                )
                .map(academicYearMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicCalendarSummaryDTO getAcademicCalendarSummary() {
        AcademicYear currentYear = academicYearRepository.findByCurrentTrueAndActiveTrue().orElse(null);
        AcademicTerm currentTerm = academicTermRepository.findByCurrentTrueAndActiveTrue().orElse(null);
        AcademicTerm nextTerm = currentYear == null
                ? null
                : academicTermRepository
                        .findFirstByAcademicYearIdAndActiveTrueAndStartDateAfterOrderByStartDateAsc(
                                currentYear.getId(),
                                LocalDate.now()
                        )
                        .orElse(null);
        long activeTermCount = currentYear == null
                ? academicTermRepository.findByActiveTrue().size()
                : academicTermRepository.countByAcademicYearIdAndActiveTrue(currentYear.getId());

        return new AcademicCalendarSummaryDTO(
                currentYear == null ? null : academicYearMapper.toDTO(currentYear),
                currentTerm == null ? null : toTermDTO(currentTerm),
                nextTerm == null ? null : toTermDTO(nextTerm),
                activeTermCount,
                daysRemaining(currentTerm),
                progressPercent(currentTerm),
                buildCalendarWarnings(currentYear, currentTerm, activeTermCount)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearDTO getAcademicYearById(Long id) {
        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        return academicYearMapper.toDTO(academicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicYearDTO> searchAcademicYears(String name, Pageable pageable) {
        return academicYearRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(academicYearMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicYearDTO> getAllActiveAcademicYears() {
        return academicYearRepository.findByActiveTrue()
                .stream()
                .map(academicYearMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearDTO getCurrentAcademicYear() {
        AcademicYear academicYear = academicYearRepository.findByCurrentTrueAndActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Current academic year not found"));

        return academicYearMapper.toDTO(academicYear);
    }

    @Override
    public AcademicYearDTO setCurrentAcademicYear(Long id) {
        AcademicYear selectedAcademicYear = academicYearRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        academicYearRepository.findByCurrentTrueAndActiveTrue()
                .ifPresent(currentAcademicYear -> currentAcademicYear.setCurrent(false));

        academicTermRepository.findByCurrentTrueAndActiveTrue()
                .filter(currentTerm -> !selectedAcademicYear.getId().equals(currentTerm.getAcademicYear().getId()))
                .ifPresent(currentTerm -> currentTerm.setCurrent(false));

        selectedAcademicYear.setCurrent(true);
        ensurePrimaryClasses(selectedAcademicYear);
        return academicYearMapper.toDTO(selectedAcademicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean overlapsExistingAcademicYear(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return academicYearRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private void validateUniqueName(String name) {
        if (academicYearRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Academic year already exists with name: " + name);
        }
    }

    private void clearTermsForAcademicYear(Long academicYearId) {
        academicTermRepository.findByAcademicYearIdAndActiveTrue(academicYearId)
                .forEach(this::clearTerm);
    }

    private void clearTerm(AcademicTerm academicTerm) {
        academicTerm.setCurrent(false);
        academicTerm.setActive(false);
    }

    private AcademicTermDTO toTermDTO(AcademicTerm term) {
        AcademicTermDTO dto = new AcademicTermDTO();
        dto.setId(term.getId());
        dto.setAcademicYearId(term.getAcademicYear().getId());
        dto.setAcademicYearName(term.getAcademicYear().getName());
        dto.setName(term.getName());
        dto.setStartDate(term.getStartDate());
        dto.setEndDate(term.getEndDate());
        dto.setCurrent(term.isCurrent());
        dto.setActive(term.isActive());
        dto.setCreatedAt(term.getCreatedAt());
        dto.setUpdatedAt(term.getUpdatedAt());
        return dto;
    }

    private Long daysRemaining(AcademicTerm term) {
        if (term == null || term.getEndDate() == null) {
            return null;
        }

        return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), term.getEndDate()));
    }

    private Integer progressPercent(AcademicTerm term) {
        if (term == null || term.getStartDate() == null || term.getEndDate() == null) {
            return null;
        }

        long totalDays = Math.max(1, ChronoUnit.DAYS.between(term.getStartDate(), term.getEndDate()));
        long elapsedDays = Math.min(Math.max(0, ChronoUnit.DAYS.between(term.getStartDate(), LocalDate.now())), totalDays);
        return (int) Math.round((elapsedDays * 100.0) / totalDays);
    }

    private List<String> buildCalendarWarnings(AcademicYear currentYear, AcademicTerm currentTerm, long activeTermCount) {
        List<String> warnings = new ArrayList<>();

        if (currentYear == null) {
            warnings.add("No current academic year is set");
        }

        if (currentYear != null && activeTermCount == 0) {
            warnings.add("Current academic year has no active terms");
        }

        if (currentYear != null && currentTerm == null) {
            warnings.add("No current academic term is set");
        }

        return warnings;
    }

    private void ensurePrimaryClasses(AcademicYear academicYear) {
        for (int level = 1; level <= 5; level++) {
            Grade grade = ensurePrimaryGrade(level);
            for (ClassSection section : ClassSection.values()) {
                ensurePrimaryClass(academicYear, grade, section);
            }
        }
    }

    private Grade ensurePrimaryGrade(int level) {
        return gradeRepository.findByLevel(level)
                .map(grade -> {
                    grade.setName("Grade " + level);
                    grade.setActive(true);
                    return grade;
                })
                .orElseGet(() -> {
                    Grade grade = new Grade();
                    grade.setName("Grade " + level);
                    grade.setLevel(level);
                    grade.setActive(true);
                    return gradeRepository.save(grade);
                });
    }

    private void ensurePrimaryClass(AcademicYear academicYear, Grade grade, ClassSection section) {
        String sectionName = section.name();
        classRepository.findByAcademicYearIdAndGradeIdAndSection(academicYear.getId(), grade.getId(), sectionName)
                .ifPresentOrElse(existingClass -> {
                    existingClass.setAcademicYear(academicYear);
                    existingClass.setGrade(grade);
                    existingClass.setSection(sectionName);
                    existingClass.setName(grade.getName() + " " + sectionName);
                    existingClass.setActive(true);
                }, () -> {
                    org.edu.entity.Class studentClass = new org.edu.entity.Class();
                    studentClass.setAcademicYear(academicYear);
                    studentClass.setGrade(grade);
                    studentClass.setSection(sectionName);
                    studentClass.setName(grade.getName() + " " + sectionName);
                    studentClass.setActive(true);
                    classRepository.save(studentClass);
                });
    }
}
