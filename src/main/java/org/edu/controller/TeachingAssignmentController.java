package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.TeachingAssignmentDTO;
import org.edu.service.TeachingAssignmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teaching-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Teaching Assignments", description = "Manage teacher class-subject assignments")
@SecurityRequirement(name = "bearerAuth")
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    @PostMapping
    @Operation(summary = "Create a teaching assignment")
    public TeachingAssignmentDTO createAssignment(@Valid @RequestBody TeachingAssignmentDTO dto) {
        return teachingAssignmentService.createAssignment(dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a teaching assignment")
    public TeachingAssignmentDTO updateAssignment(@PathVariable Long id, @Valid @RequestBody TeachingAssignmentDTO dto) {
        return teachingAssignmentService.updateAssignment(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a teaching assignment")
    public void deactivateAssignment(@PathVariable Long id) {
        teachingAssignmentService.deactivateAssignment(id);
    }

    @GetMapping
    @Operation(summary = "List teaching assignments")
    public Page<TeachingAssignmentDTO> getAssignments(Pageable pageable) {
        return teachingAssignmentService.getAssignments(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a teaching assignment by id")
    public TeachingAssignmentDTO getAssignmentById(@PathVariable Long id) {
        return teachingAssignmentService.getAssignmentById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search teaching assignments")
    public Page<TeachingAssignmentDTO> searchAssignments(@RequestParam String keyword, Pageable pageable) {
        return teachingAssignmentService.searchAssignments(keyword, pageable);
    }

    @GetMapping("/teacher/{staffId}")
    @Operation(summary = "List assignments for one teacher")
    public List<TeachingAssignmentDTO> getAssignmentsByTeacher(@PathVariable Long staffId) {
        return teachingAssignmentService.getAssignmentsByTeacher(staffId);
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "List assignments for one class")
    public List<TeachingAssignmentDTO> getAssignmentsByClass(@PathVariable Long classId) {
        return teachingAssignmentService.getAssignmentsByClass(classId);
    }
}
