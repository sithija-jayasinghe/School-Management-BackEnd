package org.edu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.GradeDTO;
import org.edu.entity.Grade;
import org.edu.repository.GradeRepository;
import org.edu.service.GradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

    @Override
    public List<GradeDTO> getAllActiveGrades() {
        return gradeRepository.findByActiveTrueOrderByLevelAsc()
                .stream()
                .map(this::toDTO)
                .toList();
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
