package org.edu.service;

import org.edu.dto.StudentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    StudentDTO createStudent(StudentDTO studentDTO);
    StudentDTO updateStudent(Long id,StudentDTO studentDTO);
    void deleteStudent(Long id); //For Dev : Soft delete (sets active = false)
    Page<StudentDTO> getAllStudents(Pageable pageable);
    StudentDTO getStudentById(Long id);
    Page<StudentDTO> searchStudents(String name, Pageable pageable);
    List<StudentDTO> getAllActiveStudents();
    List<StudentDTO> getStudentsByClassId(Long classId);
}
