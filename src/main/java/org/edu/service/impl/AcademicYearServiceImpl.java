package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicYearDTO;
import org.edu.entity.AcademicYear;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AcademicYearMapper;
import org.edu.repository.AcademicYearRepository;
import org.edu.service.AcademicYearService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
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

        return academicYearMapper.toDTO(academicYearRepository.save(academicYear));
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

        selectedAcademicYear.setCurrent(true);
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
}
