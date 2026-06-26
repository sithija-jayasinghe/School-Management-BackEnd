package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ParentDTO;
import org.edu.dto.ParentStudentDTO;
import org.edu.dto.request.ParentStudentRequest;
import org.edu.service.AuditLogService;
import org.edu.service.ParentService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "Manage parent records and parent-student relationships")
@SecurityRequirement(name = "bearerAuth")
public class ParentController {

    private final ParentService parentService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create a parent")
    public ParentDTO createParent(@Valid @RequestBody ParentDTO parentDTO) {
        ParentDTO response = parentService.createParent(parentDTO);
        auditLogService.log(AuditAction.CREATE, AuditEntityType.PARENT, response.getId(), response.getName(), "Created parent record");
        return response;
    }

    @GetMapping
    @Operation(summary = "List parents")
    public Page<ParentDTO> getAllParents(Pageable pageable) {
        return parentService.getAllParents(pageable);
    }

    @GetMapping("/{parentId}")
    @Operation(summary = "Get a parent by id")
    public ParentDTO getParentById(@PathVariable Long parentId) {
        return parentService.getParentById(parentId);
    }

    @PatchMapping("/{parentId}")
    @Operation(summary = "Update a parent")
    public ParentDTO updateParent(@PathVariable Long parentId,
                                  @RequestBody ParentDTO parentDTO) {
        ParentDTO response = parentService.updateParent(parentId, parentDTO);
        auditLogService.log(AuditAction.UPDATE, AuditEntityType.PARENT, response.getId(), response.getName(), "Updated parent record");
        return response;
    }

    @DeleteMapping("/{parentId}")
    @Operation(summary = "Deactivate a parent")
    public void deleteParent(@PathVariable Long parentId) {
        parentService.deleteParent(parentId);
        auditLogService.log(AuditAction.DELETE, AuditEntityType.PARENT, parentId, "Parent #" + parentId, "Deleted parent record");
    }

    @GetMapping("/search")
    @Operation(summary = "Search parents by keyword")
    public Page<ParentDTO> searchParents(@RequestParam String keyword,
                                         Pageable pageable) {
        return parentService.searchParents(keyword, pageable);
    }

    @GetMapping("/active")
    @Operation(summary = "List active parents")
    public List<ParentDTO> getActiveParents() {
        return parentService.getAllActiveParents();
    }

    @GetMapping("/inactive")
    @Operation(summary = "List inactive parents")
    public List<ParentDTO> getInactiveParents() {
        return parentService.getAllInactiveParents();
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get a parent by user id")
    public ParentDTO getParentByUserId(@PathVariable Long userId) {
        return parentService.getParentByUserId(userId);
    }

    @PostMapping("/{parentId}/activate")
    @Operation(summary = "Activate a parent")
    public void activateParent(@PathVariable Long parentId) {
        parentService.activateParent(parentId);
        auditLogService.log(AuditAction.ACTIVATE, AuditEntityType.PARENT, parentId, "Parent #" + parentId, "Activated parent record");
    }

    @PostMapping("/{parentId}/deactivate")
    @Operation(summary = "Deactivate a parent")
    public void deactivateParent(@PathVariable Long parentId) {
        parentService.deactivateParent(parentId);
        auditLogService.log(AuditAction.DEACTIVATE, AuditEntityType.PARENT, parentId, "Parent #" + parentId, "Deactivated parent record");
    }

    // Parent-Student Relationship APIs

    @PostMapping("/{parentId}/students/{studentId}")
    @Operation(summary = "Link a parent to a student")
    public ParentStudentDTO linkParentToStudent(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @Valid @RequestBody ParentStudentRequest request) {
        ParentStudentDTO response = parentService.linkParentToStudent(parentId, studentId, request);
        auditLogService.log(
            AuditAction.LINK,
            AuditEntityType.PARENT_STUDENT_LINK,
            response.getId(),
            "Parent " + parentId + " -> Student " + studentId,
            "Linked parent to student as " + response.getRelationshipType()
        );
        return response;
    }

    @DeleteMapping("/{parentId}/students/{studentId}")
    @Operation(summary = "Unlink a parent from a student")
    public void unlinkParentFromStudent(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.unlinkParentFromStudent(parentId, studentId);
        auditLogService.log(
            AuditAction.UNLINK,
            AuditEntityType.PARENT_STUDENT_LINK,
            null,
            "Parent " + parentId + " -> Student " + studentId,
            "Removed parent-student relationship"
        );
    }

    @GetMapping("/{parentId}/students")
    @Operation(summary = "List students linked to a parent")
    public List<ParentStudentDTO> getStudentsByParent(@PathVariable Long parentId) {
        return parentService.getStudentsByParent(parentId);
    }

    @GetMapping("/{parentId}/students/{studentId}/relationship")
    @Operation(summary = "Get a parent-student relationship")
    public ParentStudentDTO getRelationship(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        return parentService.getRelationship(parentId, studentId);
    }

    @PatchMapping("/{parentId}/students/{studentId}/relationship")
    @Operation(summary = "Update a parent-student relationship")
    public ParentStudentDTO updateRelationship(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @Valid @RequestBody ParentStudentRequest request) {
        ParentStudentDTO response = parentService.updateRelationship(parentId, studentId, request);
        auditLogService.log(
            AuditAction.UPDATE,
            AuditEntityType.PARENT_STUDENT_LINK,
            response.getId(),
            "Parent " + parentId + " -> Student " + studentId,
            "Updated parent-student relationship to " + response.getRelationshipType()
        );
        return response;
    }

    @PostMapping("/{parentId}/students/{studentId}/set-primary-contact")
    @Operation(summary = "Mark a parent as the primary contact for a student")
    public void setPrimaryContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.setPrimaryContact(parentId, studentId);
    }

    @PostMapping("/{parentId}/students/{studentId}/set-emergency-contact")
    @Operation(summary = "Mark a parent as the emergency contact for a student")
    public void setEmergencyContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.setEmergencyContact(parentId, studentId);
    }

    @PostMapping("/{parentId}/students/{studentId}/remove-primary-contact")
    @Operation(summary = "Remove primary contact status from a parent-student link")
    public void removePrimaryContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.removePrimaryContact(parentId, studentId);
    }

    @PostMapping("/{parentId}/students/{studentId}/remove-emergency-contact")
    @Operation(summary = "Remove emergency contact status from a parent-student link")
    public void removeEmergencyContact(
            @PathVariable Long parentId,
            @PathVariable Long studentId) {
        parentService.removeEmergencyContact(parentId, studentId);
    }
}
