package org.edu.service;

import java.util.List;
import org.edu.dto.GradeDTO;

public interface GradeService {

    List<GradeDTO> getAllActiveGrades();
}
