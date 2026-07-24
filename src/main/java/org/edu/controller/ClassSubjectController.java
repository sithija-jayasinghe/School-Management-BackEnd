package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.dto.SubjectDTO;
import org.edu.service.SubjectService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Class Subjects", description = "Manage class and subject assignment relationships")
@SecurityRequirement(name = "bearerAuth")
public class ClassSubjectController {

    private final SubjectService subjectService;

    @PostMapping("/api/classes/{classId}/subjects/{subjectId}")
    @Operation(summary = "Assign a subject to a class")
    public void assignSubjectToClass(@PathVariable Long classId, @PathVariable Long subjectId) {
        subjectService.assignSubjectToClass(classId, subjectId);
    }

    @DeleteMapping("/api/classes/{classId}/subjects/{subjectId}")
    @Operation(summary = "Remove a subject from a class")
    public void removeSubjectFromClass(@PathVariable Long classId, @PathVariable Long subjectId) {
        subjectService.removeSubjectFromClass(classId, subjectId);
    }

    @GetMapping("/api/classes/{classId}/subjects")
    @Operation(summary = "List subjects assigned to a class")
    public List<SubjectDTO> getSubjectsByClass(@PathVariable Long classId) {
        return subjectService.getSubjectsByClass(classId);
    }

    @GetMapping("/api/subjects/{subjectId}/classes")
    @Operation(summary = "List classes assigned to a subject")
    public List<ClassDTO> getClassesBySubject(@PathVariable Long subjectId) {
        return subjectService.getClassesBySubject(subjectId);
    }
}
