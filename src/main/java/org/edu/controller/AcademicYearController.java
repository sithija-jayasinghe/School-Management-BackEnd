package org.edu.controller;

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
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final AcademicTermService academicTermService;

    @PostMapping
    public AcademicYearDTO createAcademicYear(@Valid @RequestBody AcademicYearDTO academicYearDTO) {
        return academicYearService.createAcademicYear(academicYearDTO);
    }

    @PatchMapping("/{id}")
    public AcademicYearDTO updateAcademicYear(@PathVariable Long id,
                                              @RequestBody AcademicYearDTO academicYearDTO) {
        return academicYearService.updateAcademicYear(id, academicYearDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
    }

    @PostMapping("/{id}/activate-academic-year")
    public void activateAcademicYear(@PathVariable Long id) {
        academicYearService.activateAcademicYear(id);
    }

    @PostMapping("/{id}/deactivate-academic-year")
    public void deactivateAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
    }

    @PostMapping("/{id}/close-academic-year")
    public void closeAcademicYear(@PathVariable Long id) {
        academicYearService.closeAcademicYear(id);
    }

    @GetMapping
    public Page<AcademicYearDTO> getAllAcademicYears(Pageable pageable) {
        return academicYearService.getAllAcademicYears(pageable);
    }

    @GetMapping("/{id}")
    public AcademicYearDTO getAcademicYearById(@PathVariable Long id) {
        return academicYearService.getAcademicYearById(id);
    }

    @GetMapping("/search-academic-years")
    public Page<AcademicYearDTO> searchAcademicYears(@RequestParam String name,
                                                     Pageable pageable) {
        return academicYearService.searchAcademicYears(name, pageable);
    }

    @GetMapping("/active-academic-years")
    public List<AcademicYearDTO> getActiveAcademicYears() {
        return academicYearService.getAllActiveAcademicYears();
    }

    @GetMapping("/current-academic-year")
    public AcademicYearDTO getCurrentAcademicYear() {
        return academicYearService.getCurrentAcademicYear();
    }

    @PostMapping("/{id}/set-as-current-academic-year")
    public AcademicYearDTO setCurrentAcademicYear(@PathVariable Long id) {
        return academicYearService.setCurrentAcademicYear(id);
    }

    @GetMapping("/{id}/academic-terms")
    public List<AcademicTermDTO> getTermsByAcademicYear(@PathVariable Long id) {
        return academicTermService.getTermsByAcademicYear(id);
    }

    @GetMapping("/{id}/active-academic-terms")
    public List<AcademicTermDTO> getActiveTermsByAcademicYear(@PathVariable Long id) {
        return academicTermService.getActiveTermsByAcademicYear(id);
    }

    @GetMapping("/{id}/current-academic-term")
    public AcademicTermDTO getCurrentTermByAcademicYear(@PathVariable Long id) {
        return academicTermService.getCurrentAcademicTermByAcademicYear(id);
    }
}
