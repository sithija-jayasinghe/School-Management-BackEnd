package org.edu.controller;

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
public class ClassController {

    private final ClassService classService;
    private final StudentService studentService;

    @PostMapping
    public ClassDTO createClass(@Valid @RequestBody ClassDTO classDTO) {
        return classService.createClass(classDTO);
    }

    @PatchMapping("/{id}")
    public ClassDTO updateClass(@PathVariable Long id,
                                @RequestBody ClassDTO classDTO) {
        return classService.updateClass(id, classDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
    }

    @GetMapping
    public Page<ClassDTO> getAllClasses(Pageable pageable) {
        return classService.getAllClasses(pageable);
    }

    @GetMapping("/{id}")
    public ClassDTO getClassById(@PathVariable Long id) {
        return classService.getClassById(id);
    }

    @GetMapping("/search")
    public Page<ClassDTO> searchClasses(@RequestParam String name,
                                        Pageable pageable) {
        return classService.searchClasses(name, pageable);
    }

    @GetMapping("/active")
    public List<ClassDTO> getActiveClasses() {
        return classService.getAllActiveClasses();
    }

    @GetMapping("/{id}/students")
    public List<StudentDTO> getStudentsByClass(@PathVariable Long id) {
        return studentService.getStudentsByClassId(id);
    }
}
