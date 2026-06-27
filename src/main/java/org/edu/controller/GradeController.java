package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.GradeDTO;
import org.edu.service.GradeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/active")
    @Operation(summary = "List active grades")
    public List<GradeDTO> getActiveGrades() {
        return gradeService.getAllActiveGrades();
    }
}
