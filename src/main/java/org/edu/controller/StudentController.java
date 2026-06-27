package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.dto.StudentEnrollmentDTO;
import org.edu.dto.ParentStudentDTO;
import org.edu.service.StudentService;
import org.edu.service.ParentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Manage student records and parent relationships")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;
    private final ParentService parentService;

    @PostMapping
    @Operation(summary = "Create a student")
    public StudentDTO createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        return studentService.createStudent(studentDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a student")
    public StudentDTO updateStudent(@PathVariable Long id,
                                    @RequestBody StudentDTO studentDTO) {
        return studentService.updateStudent(id, studentDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a student")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }

    @GetMapping
    @Operation(summary = "List students")
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        return studentService.getAllStudents(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a student by id")
    public StudentDTO getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search students by name")
    public Page<StudentDTO> searchStudents(@RequestParam String name,
                                           Pageable pageable) {
        return studentService.searchStudents(name, pageable);
    }

    @GetMapping("/active")
    @Operation(summary = "List active students")
    public List<StudentDTO> getActiveStudents() {
        return studentService.getAllActiveStudents();
    }

    @GetMapping("/{id}/parents")
    @Operation(summary = "List parents linked to a student")
    public List<ParentStudentDTO> getParentsByStudent(@PathVariable Long id) {
        return parentService.getParentsByStudent(id);
    }

    @GetMapping("/{id}/enrollments")
    @Operation(summary = "List enrollment history for a student")
    public List<StudentEnrollmentDTO> getStudentEnrollments(@PathVariable Long id) {
        return studentService.getStudentEnrollments(id);
    }
}
