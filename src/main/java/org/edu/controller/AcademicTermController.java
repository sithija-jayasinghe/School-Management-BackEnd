package org.edu.controller;

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
public class AcademicTermController {

    private final AcademicTermService academicTermService;

    @PostMapping
    public AcademicTermDTO createAcademicTerm(@Valid @RequestBody AcademicTermDTO academicTermDTO) {
        return academicTermService.createAcademicTerm(academicTermDTO);
    }

    @PatchMapping("/{id}")
    public AcademicTermDTO updateAcademicTerm(@PathVariable Long id,
                                              @RequestBody AcademicTermDTO academicTermDTO) {
        return academicTermService.updateAcademicTerm(id, academicTermDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteAcademicTerm(@PathVariable Long id) {
        academicTermService.deleteAcademicTerm(id);
    }

    @PostMapping("/{id}/activate-academic-term")
    public void activateAcademicTerm(@PathVariable Long id) {
        academicTermService.activateAcademicTerm(id);
    }

    @PostMapping("/{id}/deactivate-academic-term")
    public void deactivateAcademicTerm(@PathVariable Long id) {
        academicTermService.deleteAcademicTerm(id);
    }

    @PostMapping("/{id}/close-academic-term")
    public void closeAcademicTerm(@PathVariable Long id) {
        academicTermService.closeAcademicTerm(id);
    }

    @GetMapping
    public Page<AcademicTermDTO> getAllAcademicTerms(Pageable pageable) {
        return academicTermService.getAllAcademicTerms(pageable);
    }

    @GetMapping("/{id}")
    public AcademicTermDTO getAcademicTermById(@PathVariable Long id) {
        return academicTermService.getAcademicTermById(id);
    }

    @GetMapping("/search-academic-terms")
    public Page<AcademicTermDTO> searchAcademicTerms(@RequestParam String name,
                                                     Pageable pageable) {
        return academicTermService.searchAcademicTerms(name, pageable);
    }

    @GetMapping("/active-academic-terms")
    public List<AcademicTermDTO> getActiveAcademicTerms() {
        return academicTermService.getAllActiveAcademicTerms();
    }

    @GetMapping("/current-academic-term")
    public AcademicTermDTO getCurrentAcademicTerm() {
        return academicTermService.getCurrentAcademicTerm();
    }

    @PostMapping("/{id}/set-as-current-academic-term")
    public AcademicTermDTO setCurrentAcademicTerm(@PathVariable Long id) {
        return academicTermService.setCurrentAcademicTerm(id);
    }
}
