package org.edu.service;

import org.edu.dto.ClassDTO;
import org.edu.dto.SubjectDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SubjectService {
    SubjectDTO createSubjects(SubjectDTO subjectDTO);
    SubjectDTO updateSubjects(Long id,SubjectDTO subjectDTO);
    void deleteSubjects(Long id);
    Page<SubjectDTO> getAllSubjects(Pageable pageable);
    Page<SubjectDTO> filterSubjects(Map<String, String> filters, Pageable pageable);
    SubjectDTO getSubjectById(Long id);
    Page<SubjectDTO> searchSubjects(String keyword, Pageable pageable);
    SubjectDTO getSubjectsByCodeId(String code);
    void assignSubjectToClass(Long classId, Long subjectId);
    void removeSubjectFromClass(Long classId, Long subjectId);
    List<SubjectDTO> getSubjectsByClass(Long id);
    List<ClassDTO> getClassesBySubject(Long id);
}
