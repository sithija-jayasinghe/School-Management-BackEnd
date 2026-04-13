package org.edu.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.edu.dto.StudentDTO;
import org.edu.entity.Student;
import org.junit.jupiter.api.Test;

class StudentMapperTest {

    private final StudentMapper studentMapper = new StudentMapper();

    @Test
    void shouldMapDtoToEntity() {
        StudentDTO dto = new StudentDTO();
        dto.setId(1L);
        dto.setName("Nethma");
        dto.setDateOfBirth(LocalDate.of(2000, 4, 10));
        dto.setActive(true);
        dto.setEmail("nethma@gmail.com");
        dto.setPhoneNumber("0728701707");
        dto.setCreatedAt(LocalDateTime.of(2026, 4, 13, 22, 0));
        dto.setUpdatedAt(LocalDateTime.of(2026, 4, 13, 22, 30));

        Student student = studentMapper.toEntity(dto);

        assertEquals(dto.getId(), student.getId());
        assertEquals(dto.getName(), student.getName());
        assertEquals(dto.getDateOfBirth(), student.getDateOfBirth());
        assertTrue(student.isActive());
        assertEquals(dto.getEmail(), student.getEmail());
        assertEquals(dto.getPhoneNumber(), student.getPhoneNumber());
        assertEquals(dto.getCreatedAt(), student.getCreatedAt());
        assertEquals(dto.getUpdatedAt(), student.getUpdatedAt());
    }
}
