package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.entity.Student;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StudentRepository;
import org.edu.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {

        Student student =  studentMapper.toEntity(studentDTO);
        student.setActive(true);

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toDTO(savedStudent);
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {

        Student student = studentRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Student not found with id: " + id)
        );


        if (studentDTO.getName() != null) {
            student.setName(studentDTO.getName());
        }

        if (studentDTO.getDateOfBirth() != null) {
            student.setDateOfBirth(studentDTO.getDateOfBirth());
        }

        if (studentDTO.getEmail() != null) {
            student.setEmail(studentDTO.getEmail());
        }

        if (studentDTO.getPhoneNumber() != null) {
            student.setPhoneNumber(studentDTO.getPhoneNumber());
        }

        Student updatedStudent = studentRepository.save(student);
        return studentMapper.toDTO(updatedStudent);
    }

    @Override
    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Student not found with id: " + id)
        );

        student.setActive(false);
        studentRepository.save(student);
    }

    @Override
    public Page<StudentDTO> getAllStudents(Pageable pageable) {

        return studentRepository.findAll(pageable).map(studentMapper::toDTO);
    }

    @Override
    public StudentDTO getStudentById(Long id) {

        Student student = studentRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Student not found with id: " + id)
        );
        return studentMapper.toDTO(student);
    }

    @Override
    public Page<StudentDTO> searchStudents(String name, Pageable pageable) {

        return studentRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
            .map(studentMapper::toDTO);
    }

    @Override
    public List<StudentDTO> getAllActiveStudents() {

        List<Student> activeStudents = studentRepository.findByActiveTrue();

        return activeStudents.stream()
            .map(studentMapper::toDTO)
            .toList();
    }
}
