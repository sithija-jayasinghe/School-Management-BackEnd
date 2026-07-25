package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.SubjectDTO;
import org.edu.service.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Subjects", description = "Manage subjects and subject lookup operations")
@SecurityRequirement(name = "bearerAuth")
public class SubjectController {

        private final SubjectService subjectService;

        @PostMapping
        @Operation(summary = "Create a subject")
        public SubjectDTO createSubjects(@Valid @RequestBody SubjectDTO dto) {
            return subjectService.createSubjects(dto);
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
        @Operation(summary = "List subjects")
        public Page<SubjectDTO> getAllSubjects(
                @RequestParam(required = false) String keyword,
                @RequestParam(required = false) Long gradeId,
                @RequestParam(required = false) Boolean hasClassCoverage,
                Pageable pageable
        ) {
            if ((keyword != null && !keyword.isBlank()) || gradeId != null || hasClassCoverage != null) {
                return subjectService.filterSubjects(keyword, gradeId, hasClassCoverage, pageable);
            }

            return subjectService.getAllSubjects(pageable);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
        @Operation(summary = "Get a subject by id")
        public SubjectDTO getSubjectById(@PathVariable Long id) {
            return subjectService.getSubjectById(id);
        }

        @PatchMapping("/{id}")
        @Operation(summary = "Update a subject")
        public SubjectDTO updateSubjects(@PathVariable Long id, @Valid @RequestBody SubjectDTO dto) {
            return subjectService.updateSubjects(id, dto);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a subject")
        public void deleteSubjects(@PathVariable Long id) {
            subjectService.deleteSubjects(id);
        }

        @GetMapping("/search")
        @Operation(summary = "Search subjects by keyword")
        public Page<SubjectDTO> searchSubjects(@RequestParam String keyword, Pageable pageable) {
            return subjectService.searchSubjects(keyword, pageable);
        }

        @GetMapping("/code/{code}")
        @Operation(summary = "Get a subject by code")
        public SubjectDTO getSubjectsByCodeId(@PathVariable String code) {
            return subjectService.getSubjectsByCodeId(code);
        }

}
