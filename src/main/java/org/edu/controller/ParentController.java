package org.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ParentDTO;
import org.edu.dto.ParentStudentDTO;
import org.edu.dto.request.ParentStudentRequest;
import org.edu.service.ParentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    public ParentDTO createParent(@Valid @RequestBody ParentDTO parentDTO) {
        return parentService.createParent(parentDTO);
    }

    @GetMapping
    public Page<ParentDTO> getAllParents(Pageable pageable) {
        return parentService.getAllParents(pageable);
    }

    @GetMapping("/{parentId}")
    public ParentDTO getParentById(@PathVariable Long parentId) {
        return parentService.getParentById(parentId);
    }

    @PatchMapping("/{parentId}")
    public ParentDTO updateParent(@PathVariable Long parentId,
                                  @RequestBody ParentDTO parentDTO) {
        return parentService.updateParent(parentId, parentDTO);
    }

    @DeleteMapping("/{parentId}")
    public void deleteParent(@PathVariable Long parentId) {
        parentService.deleteParent(parentId);
    }

    @GetMapping("/search")
    public Page<ParentDTO> searchParents(@RequestParam String keyword,
                                         Pageable pageable) {
        return parentService.searchParents(keyword, pageable);
    }

    @GetMapping("/active")
    public List<ParentDTO> getActiveParents() {
        return parentService.getAllActiveParents();
    }

    @GetMapping("/inactive")
    public List<ParentDTO> getInactiveParents() {
        return parentService.getAllInactiveParents();
    }

    @GetMapping("/by-user/{userId}")
    public ParentDTO getParentByUserId(@PathVariable Long userId) {
        return parentService.getParentByUserId(userId);
    }

    @PostMapping("/{parentId}/activate")
    public void activateParent(@PathVariable Long parentId) {
        parentService.activateParent(parentId);
    }

    @PostMapping("/{parentId}/deactivate")
    public void deactivateParent(@PathVariable Long parentId) {
        parentService.deactivateParent(parentId);
    }

    // Parent-Student Relationship APIs

    @PostMapping("/{parentId}/students/{studentId}")
    public ParentStudentDTO linkParentToStudent(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @Valid @RequestBody ParentStudentRequest request) {
        return parentService.linkParentToStudent(parentId, studentId, request);
    }

    @DeleteMapping("/{parentId}/students/{studentId}")
    public void unlinkParentFromStudent(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.unlinkParentFromStudent(parentId, studentId);
    }

    @GetMapping("/{parentId}/students")
    public List<ParentStudentDTO> getStudentsByParent(@PathVariable Long parentId) {
        return parentService.getStudentsByParent(parentId);
    }

    @GetMapping("/{parentId}/students/{studentId}/relationship")
    public ParentStudentDTO getRelationship(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        return parentService.getRelationship(parentId, studentId);
    }

    @PatchMapping("/{parentId}/students/{studentId}/relationship")
    public ParentStudentDTO updateRelationship(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @Valid @RequestBody ParentStudentRequest request) {
        return parentService.updateRelationship(parentId, studentId, request);
    }

    @PostMapping("/{parentId}/students/{studentId}/set-primary-contact")
    public void setPrimaryContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.setPrimaryContact(parentId, studentId);
    }

    @PostMapping("/{parentId}/students/{studentId}/set-emergency-contact")
    public void setEmergencyContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.setEmergencyContact(parentId, studentId);
    }

    @PostMapping("/{parentId}/students/{studentId}/remove-primary-contact")
    public void removePrimaryContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.removePrimaryContact(parentId, studentId);
    }

    @PostMapping("/{parentId}/students/{studentId}/remove-emergency-contact")
    public void removeEmergencyContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.removeEmergencyContact(parentId, studentId);
    }
}
