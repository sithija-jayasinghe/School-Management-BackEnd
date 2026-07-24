package org.edu.service;

import org.edu.dto.StudentDTO;
import org.edu.dto.StudentEnrollmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    StudentDTO createStudent(StudentDTO studentDTO);
    StudentDTO updateStudent(Long id,StudentDTO studentDTO);
    void deleteStudent(Long id); //For Dev : Soft delete (sets active = false)
    Page<StudentDTO> getAllStudents(Pageable pageable);
    Page<StudentDTO> filterStudents(String keyword, Long classId, Long houseId, Boolean active, Pageable pageable);
    StudentDTO getStudentById(Long id);
    Page<StudentDTO> searchStudents(String name, Pageable pageable);
    List<StudentDTO> getAllActiveStudents();
    List<StudentDTO> getStudentsByClassId(Long classId);
    List<StudentEnrollmentDTO> getStudentEnrollments(Long studentId);
    StudentDTO transferStudent(Long studentId, Long classId);
    StudentDTO promoteStudent(Long studentId, Long classId);
    StudentDTO withdrawStudent(Long studentId);
    StudentDTO completeStudent(Long studentId);
}
