package org.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public StudentDTO createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        return studentService.createStudent(studentDTO);
    }

    @PatchMapping("/{id}")
    public StudentDTO updateStudent(@PathVariable Long id,
                                    @RequestBody StudentDTO studentDTO) {
        return studentService.updateStudent(id, studentDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }

    @GetMapping
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        return studentService.getAllStudents(pageable);
    }

    @GetMapping("/{id}")
    public StudentDTO getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping("/search")
    public Page<StudentDTO> searchStudents(@RequestParam String name,
                                           Pageable pageable) {
        return studentService.searchStudents(name, pageable);
    }

    @GetMapping("/active")
    public List<StudentDTO> getActiveStudents() {
        return studentService.getAllActiveStudents();
    }
}
