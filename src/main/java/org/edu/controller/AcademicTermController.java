package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicTermDTO;
import org.edu.service.AcademicTermService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic-terms")
@RequiredArgsConstructor
@Tag(name = "Academic Terms", description = "Manage academic term records and term lifecycle actions")
@SecurityRequirement(name = "bearerAuth")
public class AcademicTermController {

    private final AcademicTermService academicTermService;

    @PostMapping
    @Operation(summary = "Create an academic term")
    public AcademicTermDTO createAcademicTerm(@Valid @RequestBody AcademicTermDTO academicTermDTO) {
        return academicTermService.createAcademicTerm(academicTermDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an academic term")
    public AcademicTermDTO updateAcademicTerm(@PathVariable Long id,
                                              @RequestBody AcademicTermDTO academicTermDTO) {
        return academicTermService.updateAcademicTerm(id, academicTermDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an academic term")
    public void deleteAcademicTerm(@PathVariable Long id) {
        academicTermService.deleteAcademicTerm(id);
    }

    @PostMapping("/{id}/activate-academic-term")
    @Operation(summary = "Activate an academic term")
    public void activateAcademicTerm(@PathVariable Long id) {
        academicTermService.activateAcademicTerm(id);
    }

    @PostMapping("/{id}/deactivate-academic-term")
    @Operation(summary = "Deactivate an academic term using the lifecycle endpoint")
    public void deactivateAcademicTerm(@PathVariable Long id) {
        academicTermService.deleteAcademicTerm(id);
    }

    @PostMapping("/{id}/close-academic-term")
    @Operation(summary = "Close an academic term")
    public void closeAcademicTerm(@PathVariable Long id) {
        academicTermService.closeAcademicTerm(id);
    }

    @GetMapping
    @Operation(summary = "List academic terms")
    public Page<AcademicTermDTO> getAllAcademicTerms(Pageable pageable) {
        return academicTermService.getAllAcademicTerms(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an academic term by id")
    public AcademicTermDTO getAcademicTermById(@PathVariable Long id) {
        return academicTermService.getAcademicTermById(id);
    }

    @GetMapping("/search-academic-terms")
    @Operation(summary = "Search academic terms by name")
    public Page<AcademicTermDTO> searchAcademicTerms(@RequestParam String name,
                                                     Pageable pageable) {
        return academicTermService.searchAcademicTerms(name, pageable);
    }

    @GetMapping("/active-academic-terms")
    @Operation(summary = "List active academic terms")
    public List<AcademicTermDTO> getActiveAcademicTerms() {
        return academicTermService.getAllActiveAcademicTerms();
    }

    @GetMapping("/current-academic-term")
    @Operation(summary = "Get the current academic term")
    public AcademicTermDTO getCurrentAcademicTerm() {
        return academicTermService.getCurrentAcademicTerm();
    }

    @PostMapping("/{id}/set-as-current-academic-term")
    @Operation(summary = "Set an academic term as current")
    public AcademicTermDTO setCurrentAcademicTerm(@PathVariable Long id) {
        return academicTermService.setCurrentAcademicTerm(id);
    }
}
