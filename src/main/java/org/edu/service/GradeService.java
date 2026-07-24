package org.edu.service;

import java.util.List;
import org.edu.dto.GradeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GradeService {

    GradeDTO createGrade(GradeDTO gradeDTO);

    GradeDTO updateGrade(Long id, GradeDTO gradeDTO);

    void deactivateGrade(Long id);

    void activateGrade(Long id);

    Page<GradeDTO> getAllGrades(Pageable pageable);

    GradeDTO getGradeById(Long id);

    List<GradeDTO> getAllActiveGrades();
}
