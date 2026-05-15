package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicTermDTO;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AcademicTermMapper;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.service.AcademicTermService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicTermServiceImpl implements AcademicTermService {

    private final AcademicTermRepository academicTermRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermMapper academicTermMapper;

    @Override
    public AcademicTermDTO createAcademicTerm(AcademicTermDTO academicTermDTO) {
        AcademicYear academicYear = getActiveAcademicYear(academicTermDTO.getAcademicYearId());
        validateDateRange(academicTermDTO.getStartDate(), academicTermDTO.getEndDate());
        validateTermInsideAcademicYear(academicYear, academicTermDTO.getStartDate(), academicTermDTO.getEndDate());
        validateUniqueName(academicYear, academicTermDTO.getName());

        if (termOverlapsInAcademicYear(academicYear.getId(), academicTermDTO.getStartDate(), academicTermDTO.getEndDate())) {
            throw new IllegalArgumentException("Academic term date range overlaps with an existing term in the academic year");
        }

        AcademicTerm academicTerm = academicTermMapper.toEntity(academicTermDTO);
        academicTerm.setAcademicYear(academicYear);
        academicTerm.setActive(true);
        academicTerm.setCurrent(false);

        return academicTermMapper.toDTO(academicTermRepository.save(academicTerm));
    }

    @Override
    public AcademicTermDTO updateAcademicTerm(Long id, AcademicTermDTO academicTermDTO) {
        AcademicTerm academicTerm = academicTermRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));

        AcademicYear academicYear = academicTerm.getAcademicYear();
        if (academicTermDTO.getAcademicYearId() != null && !academicTermDTO.getAcademicYearId().equals(academicYear.getId())) {
            academicYear = getActiveAcademicYear(academicTermDTO.getAcademicYearId());
            academicTerm.setAcademicYear(academicYear);
        }

        LocalDate startDate = academicTermDTO.getStartDate() != null ? academicTermDTO.getStartDate() : academicTerm.getStartDate();
        LocalDate endDate = academicTermDTO.getEndDate() != null ? academicTermDTO.getEndDate() : academicTerm.getEndDate();
        validateDateRange(startDate, endDate);
        validateTermInsideAcademicYear(academicYear, startDate, endDate);

        if (academicTermDTO.getName() != null
                && !academicTermDTO.getName().equalsIgnoreCase(academicTerm.getName())
                && academicTermRepository.existsByAcademicYearAndNameIgnoreCaseAndIdNot(academicYear, academicTermDTO.getName(), id)) {
            throw new IllegalArgumentException("Academic term already exists with name: " + academicTermDTO.getName());
        }

        if (academicTermRepository.existsByAcademicYearAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                academicYear,
                endDate,
                startDate,
                id
        )) {
            throw new IllegalArgumentException("Academic term date range overlaps with an existing term in the academic year");
        }

        academicTermMapper.updateEntityFromDTO(academicTermDTO, academicTerm);

        return academicTermMapper.toDTO(academicTerm);
    }

    @Override
    public void deleteAcademicTerm(Long id) {
        AcademicTerm academicTerm = academicTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));

        if (!academicTerm.isActive()) {
            throw new IllegalStateException("Academic term already inactive");
        }

        if (academicTerm.isCurrent()) {
            throw new IllegalStateException("Current academic term cannot be deactivated");
        }

        academicTerm.setActive(false);
    }

    @Override
    public void activateAcademicTerm(Long id) {
        AcademicTerm academicTerm = academicTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));

        academicTerm.setActive(true);
    }

    @Override
    public void closeAcademicTerm(Long id) {
        AcademicTerm academicTerm = academicTermRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));

        academicTerm.setCurrent(false);
        academicTerm.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicTermDTO> getAllAcademicTerms(Pageable pageable) {
        return academicTermRepository.findByActiveTrue(pageable)
                .map(academicTermMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicTermDTO getAcademicTermById(Long id) {
        AcademicTerm academicTerm = academicTermRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));

        return academicTermMapper.toDTO(academicTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicTermDTO> searchAcademicTerms(String name, Pageable pageable) {
        return academicTermRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(academicTermMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicTermDTO> getAllActiveAcademicTerms() {
        return academicTermRepository.findByActiveTrue()
                .stream()
                .map(academicTermMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicTermDTO> getTermsByAcademicYear(Long academicYearId) {
        getActiveAcademicYear(academicYearId);
        return academicTermRepository.findByAcademicYearIdAndActiveTrue(academicYearId)
                .stream()
                .map(academicTermMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicTermDTO> getActiveTermsByAcademicYear(Long academicYearId) {
        return getTermsByAcademicYear(academicYearId);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicTermDTO getCurrentAcademicTerm() {
        AcademicTerm academicTerm = academicTermRepository.findByCurrentTrueAndActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Current academic term not found"));

        return academicTermMapper.toDTO(academicTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicTermDTO getCurrentAcademicTermByAcademicYear(Long academicYearId) {
        getActiveAcademicYear(academicYearId);
        AcademicTerm academicTerm = academicTermRepository.findByAcademicYearIdAndCurrentTrueAndActiveTrue(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Current academic term not found for academic year id: " + academicYearId));

        return academicTermMapper.toDTO(academicTerm);
    }

    @Override
    public AcademicTermDTO setCurrentAcademicTerm(Long id) {
        AcademicTerm selectedAcademicTerm = academicTermRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));

        academicTermRepository.findByCurrentTrueAndActiveTrue()
                .ifPresent(currentAcademicTerm -> currentAcademicTerm.setCurrent(false));

        selectedAcademicTerm.setCurrent(true);
        return academicTermMapper.toDTO(selectedAcademicTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean termOverlapsInAcademicYear(Long academicYearId, LocalDate startDate, LocalDate endDate) {
        AcademicYear academicYear = getActiveAcademicYear(academicYearId);
        validateDateRange(startDate, endDate);
        return academicTermRepository.existsByAcademicYearAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                academicYear,
                endDate,
                startDate
        );
    }

    private AcademicYear getActiveAcademicYear(Long academicYearId) {
        return academicYearRepository.findByIdAndActiveTrue(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private void validateTermInsideAcademicYear(AcademicYear academicYear, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(academicYear.getStartDate()) || endDate.isAfter(academicYear.getEndDate())) {
            throw new IllegalArgumentException("Academic term dates must be inside the academic year date range");
        }
    }

    private void validateUniqueName(AcademicYear academicYear, String name) {
        if (academicTermRepository.existsByAcademicYearAndNameIgnoreCase(academicYear, name)) {
            throw new IllegalArgumentException("Academic term already exists with name: " + name);
        }
    }
}
