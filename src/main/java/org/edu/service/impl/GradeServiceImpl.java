package org.edu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.GradeDTO;
import org.edu.entity.Grade;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.GradeRepository;
import org.edu.service.GradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

    @Override
    public GradeDTO createGrade(GradeDTO gradeDTO) {
        validateDuplicateName(gradeDTO.getName(), null);
        validateDuplicateLevel(gradeDTO.getLevel(), null);

        Grade grade = new Grade();
        applyFields(grade, gradeDTO);
        grade.setActive(true);
        return toDTO(gradeRepository.save(grade));
    }

    @Override
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        Grade grade = gradeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active grade not found with id: " + id));
        validateDuplicateName(gradeDTO.getName(), id);
        validateDuplicateLevel(gradeDTO.getLevel(), id);
        applyFields(grade, gradeDTO);
        grade.setActive(gradeDTO.isActive());
        return toDTO(gradeRepository.save(grade));
    }

    @Override
    public void deactivateGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
        if (!grade.isActive()) {
            throw new IllegalStateException("Grade already inactive");
        }
        grade.setActive(false);
        gradeRepository.save(grade);
    }

    @Override
    public void activateGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
        if (grade.isActive()) {
            throw new IllegalStateException("Grade already active");
        }
        grade.setActive(true);
        gradeRepository.save(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeDTO> getAllGrades(Pageable pageable) {
        return gradeRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDTO getGradeById(Long id) {
        return toDTO(gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDTO> getAllActiveGrades() {
        return gradeRepository.findByActiveTrueOrderByLevelAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void applyFields(Grade grade, GradeDTO dto) {
        grade.setName(dto.getName().trim());
        grade.setLevel(dto.getLevel());
    }

    private void validateDuplicateName(String name, Long currentId) {
        boolean exists = currentId == null
                ? gradeRepository.existsByNameIgnoreCase(name.trim())
                : gradeRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), currentId);
        if (exists) {
            throw new IllegalStateException("Grade name already exists");
        }
    }

    private void validateDuplicateLevel(Integer level, Long currentId) {
        boolean exists = currentId == null
                ? gradeRepository.existsByLevel(level)
                : gradeRepository.existsByLevelAndIdNot(level, currentId);
        if (exists) {
            throw new IllegalStateException("Grade level already exists");
        }
    }

    private GradeDTO toDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setName(grade.getName());
        dto.setLevel(grade.getLevel());
        dto.setActive(grade.isActive());
        return dto;
    }
}
