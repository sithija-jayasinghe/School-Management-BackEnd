package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.exception.InvalidAgeException;
import org.edu.exception.InvalidStudentDataException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.edu.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final UserRepository userRepository;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {

        User user = userRepository.findById(studentDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (studentRepository.existsByUser(user)) {
            throw new InvalidStudentDataException("User already assigned to a student");
        }

        if (!user.getRole().name().equals("STUDENT")) {
            throw new InvalidStudentDataException("User must have STUDENT role");
        }

        if (studentDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(3))) {
            throw new InvalidAgeException("Invalid student age: Must be at least 3 years old");
        }

        Student student = studentMapper.toEntity(studentDTO);
        student.setUser(user);
        student.setActive(true);

        return studentMapper.toDTO(studentRepository.save(student));
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (studentDTO.getName() != null) {
            student.setName(studentDTO.getName());
        }

        if (studentDTO.getDateOfBirth() != null) {
            if (studentDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(3))) {
                throw new InvalidAgeException("Invalid student age: Must be at least 3 years old");
            }
            student.setDateOfBirth(studentDTO.getDateOfBirth());
        }

        if (studentDTO.getPhoneNumber() != null) {
            student.setPhoneNumber(studentDTO.getPhoneNumber());
        }

        return studentMapper.toDTO(student);
    }

    @Override
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    public Page<StudentDTO> getAllStudents(Pageable pageable) {

        return studentRepository.findAll(pageable)
                .map(studentMapper::toDTO);
    }

    @Override
    public StudentDTO getStudentById(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        return studentMapper.toDTO(student);
    }

    @Override
    public Page<StudentDTO> searchStudents(String name, Pageable pageable) {

        return studentRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .map(studentMapper::toDTO);
    }

    @Override
    public List<StudentDTO> getAllActiveStudents() {

        return studentRepository.findAll()
                .stream()
                .map(studentMapper::toDTO)
                .toList();
    }
}