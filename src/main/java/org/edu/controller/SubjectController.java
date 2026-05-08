package org.edu.controller;

import lombok.RequiredArgsConstructor;
import org.edu.dto.SubjectDTO;
import org.edu.service.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

        private final SubjectService subjectService;

        @PostMapping
        public SubjectDTO createSubjects(@RequestBody SubjectDTO dto) {
            return subjectService.createSubjects(dto);
        }

        @GetMapping
        public Page<SubjectDTO> getAllSubjects(Pageable pageable) {
            return subjectService.getAllSubjects(pageable);
        }

        @GetMapping("/{id}")
        public SubjectDTO getSubjectById(@PathVariable Long id) {
            return subjectService.getSubjectById(id);
        }

        @PatchMapping("/{id}")
        public SubjectDTO updateSubjects(@PathVariable Long id, @RequestBody SubjectDTO dto) {
            return subjectService.updateSubjects(id, dto);
        }

        @DeleteMapping("/{id}")
        public void deleteSubjects(@PathVariable Long id) {
            subjectService.deleteSubjects(id);
        }

        @GetMapping("/search")
        public Page<SubjectDTO> searchSubjects(@RequestParam String keyword, Pageable pageable) {
            return subjectService.searchSubjects(keyword, pageable);
        }

        @GetMapping("/code/{code}")
        public SubjectDTO getSubjectsByCodeId(@PathVariable String code) {
            return subjectService.getSubjectsByCodeId(code);
        }

}
