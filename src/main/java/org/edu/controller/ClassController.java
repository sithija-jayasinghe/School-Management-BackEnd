package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.service.ClassService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.edu.dto.StudentDTO;
import org.edu.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Manage class records, teachers, and student membership")
@SecurityRequirement(name = "bearerAuth")
public class ClassController {

    private final ClassService classService;
    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Create a class")
    public ClassDTO createClass(@Valid @RequestBody ClassDTO classDTO) {
        return classService.createClass(classDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a class")
    public ClassDTO updateClass(@PathVariable Long id,
                                @RequestBody ClassDTO classDTO) {
        return classService.updateClass(id, classDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a class")
    public void deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
    }

    @GetMapping
    @Operation(summary = "List classes")
    public Page<ClassDTO> getAllClasses(Pageable pageable) {
        return classService.getAllClasses(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a class by id")
    public ClassDTO getClassById(@PathVariable Long id) {
        return classService.getClassById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search classes by name")
    public Page<ClassDTO> searchClasses(@RequestParam String name,
                                        Pageable pageable) {
        return classService.searchClasses(name, pageable);
    }

    @GetMapping("/active")
    @Operation(summary = "List active classes")
    public List<ClassDTO> getActiveClasses() {
        return classService.getAllActiveClasses();
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "List students in a class")
    public List<StudentDTO> getStudentsByClass(@PathVariable Long id) {
        return studentService.getStudentsByClassId(id);
    }
}
