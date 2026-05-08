package org.edu.controller;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.dto.SubjectDTO;
import org.edu.service.SubjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClassSubjectController {

    private final SubjectService subjectService;

    @PostMapping("/api/classes/{classId}/subjects/{subjectId}")
    public void assignSubjectToClass(@PathVariable Long classId, @PathVariable Long subjectId) {
        subjectService.assignSubjectToClass(classId, subjectId);
    }

    @DeleteMapping("/api/classes/{classId}/subjects/{subjectId}")
    public void removeSubjectFromClass(@PathVariable Long classId, @PathVariable Long subjectId) {
        subjectService.removeSubjectFromClass(classId, subjectId);
    }

    @GetMapping("/api/classes/{classId}/subjects")
    public List<SubjectDTO> getSubjectsByClass(@PathVariable Long classId) {
        return subjectService.getSubjectsByClass(classId);
    }

    @GetMapping("/api/subjects/{subjectId}/classes")
    public List<ClassDTO> getClassesBySubject(@PathVariable Long subjectId) {
        return subjectService.getClassesBySubject(subjectId);
    }
}
