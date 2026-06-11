package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicTermDTO;
import org.edu.dto.AcademicYearDTO;
import org.edu.service.AcademicTermService;
import org.edu.service.AcademicYearService;
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
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
@Tag(name = "Academic Years", description = "Manage academic year records and lifecycle actions")
@SecurityRequirement(name = "bearerAuth")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final AcademicTermService academicTermService;

    @PostMapping
    @Operation(summary = "Create an academic year")
    public AcademicYearDTO createAcademicYear(@Valid @RequestBody AcademicYearDTO academicYearDTO) {
        return academicYearService.createAcademicYear(academicYearDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an academic year")
    public AcademicYearDTO updateAcademicYear(@PathVariable Long id,
                                              @RequestBody AcademicYearDTO academicYearDTO) {
        return academicYearService.updateAcademicYear(id, academicYearDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an academic year")
    public void deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
    }

    @PostMapping("/{id}/activate-academic-year")
    @Operation(summary = "Activate an academic year")
    public void activateAcademicYear(@PathVariable Long id) {
        academicYearService.activateAcademicYear(id);
    }

    @PostMapping("/{id}/deactivate-academic-year")
    @Operation(summary = "Deactivate an academic year using the lifecycle endpoint")
    public void deactivateAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
    }

    @PostMapping("/{id}/close-academic-year")
    @Operation(summary = "Close an academic year")
    public void closeAcademicYear(@PathVariable Long id) {
        academicYearService.closeAcademicYear(id);
    }

    @GetMapping
    @Operation(summary = "List academic years")
    public Page<AcademicYearDTO> getAllAcademicYears(Pageable pageable) {
        return academicYearService.getAllAcademicYears(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an academic year by id")
    public AcademicYearDTO getAcademicYearById(@PathVariable Long id) {
        return academicYearService.getAcademicYearById(id);
    }

    @GetMapping("/search-academic-years")
    @Operation(summary = "Search academic years by name")
    public Page<AcademicYearDTO> searchAcademicYears(@RequestParam String name,
                                                     Pageable pageable) {
        return academicYearService.searchAcademicYears(name, pageable);
    }

    @GetMapping("/active-academic-years")
    @Operation(summary = "List active academic years")
    public List<AcademicYearDTO> getActiveAcademicYears() {
        return academicYearService.getAllActiveAcademicYears();
    }

    @GetMapping("/current-academic-year")
    @Operation(summary = "Get the current academic year")
    public AcademicYearDTO getCurrentAcademicYear() {
        return academicYearService.getCurrentAcademicYear();
    }

    @PostMapping("/{id}/set-as-current-academic-year")
    @Operation(summary = "Set an academic year as current")
    public AcademicYearDTO setCurrentAcademicYear(@PathVariable Long id) {
        return academicYearService.setCurrentAcademicYear(id);
    }

    @GetMapping("/{id}/academic-terms")
    @Operation(summary = "List academic terms in an academic year")
    public List<AcademicTermDTO> getTermsByAcademicYear(@PathVariable Long id) {
        return academicTermService.getTermsByAcademicYear(id);
    }

    @GetMapping("/{id}/active-academic-terms")
    @Operation(summary = "List active academic terms in an academic year")
    public List<AcademicTermDTO> getActiveTermsByAcademicYear(@PathVariable Long id) {
        return academicTermService.getActiveTermsByAcademicYear(id);
    }

    @GetMapping("/{id}/current-academic-term")
    @Operation(summary = "Get the current academic term for an academic year")
    public AcademicTermDTO getCurrentTermByAcademicYear(@PathVariable Long id) {
        return academicTermService.getCurrentAcademicTermByAcademicYear(id);
    }
}
