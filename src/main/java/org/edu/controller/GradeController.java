package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.GradeDTO;
import org.edu.service.GradeService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Grades", description = "Manage academic grade levels")
@SecurityRequirement(name = "bearerAuth")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @Operation(summary = "Create a grade")
    public GradeDTO createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        return gradeService.createGrade(gradeDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a grade")
    public GradeDTO updateGrade(@PathVariable Long id, @Valid @RequestBody GradeDTO gradeDTO) {
        return gradeService.updateGrade(id, gradeDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a grade")
    public void deactivateGrade(@PathVariable Long id) {
        gradeService.deactivateGrade(id);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a grade")
    public void activateGrade(@PathVariable Long id) {
        gradeService.activateGrade(id);
    }

    @GetMapping
    @Operation(summary = "List grades")
    public Page<GradeDTO> getAllGrades(Pageable pageable) {
        return gradeService.getAllGrades(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a grade by id")
    public GradeDTO getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id);
    }

    @GetMapping("/active")
    @Operation(summary = "List active grades")
    public List<GradeDTO> getActiveGrades() {
        return gradeService.getAllActiveGrades();
    }
}
