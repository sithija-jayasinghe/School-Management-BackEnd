package org.edu.mapper;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.entity.Student;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    public Student toEntity(StudentDTO dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();

        student.setName(dto.getName());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setActive(dto.isActive());
        student.setPhoneNumber(dto.getPhoneNumber());
        return student;
    }

    public StudentDTO toDTO(Student student) {
        if (student == null) {
            return null;
        }

        StudentDTO dto = new StudentDTO();

        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setActive(student.isActive());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setUpdatedAt(student.getUpdatedAt());

        // 🔗 Map userId
        if (student.getUser() != null) {
            dto.setUserId(student.getUser().getId());
        }

        return dto;
    }

    public void updateEntityFromDTO(StudentDTO dto, Student student) {
        if (dto == null || student == null) {
            return;
        }

        if (dto.getName() != null) {
            student.setName(dto.getName());
        }

        if (dto.getDateOfBirth() != null) {
            student.setDateOfBirth(dto.getDateOfBirth());
        }

        if (dto.getPhoneNumber() != null) {
            student.setPhoneNumber(dto.getPhoneNumber());
        }
    }
}
