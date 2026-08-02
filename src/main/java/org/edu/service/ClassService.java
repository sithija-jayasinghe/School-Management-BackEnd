package org.edu.service;

import org.edu.dto.ClassDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ClassService {

    ClassDTO createClass(ClassDTO classDTO);
    ClassDTO updateClass(Long id, ClassDTO classDTO);
    void deleteClass(Long id);
    Page<ClassDTO> getAllClasses(Pageable pageable);
    Page<ClassDTO> filterClasses(Map<String, String> filters, Pageable pageable);
    ClassDTO getClassById(Long id);
    Page<ClassDTO> searchClasses(String name, Pageable pageable);
    List<ClassDTO> getAllActiveClasses();
    List<ClassDTO> getCurrentAcademicYearActiveClasses();
}
