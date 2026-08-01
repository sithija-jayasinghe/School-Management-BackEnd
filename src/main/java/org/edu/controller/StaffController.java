package org.edu.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.StaffDTO;
import org.edu.service.AuditLogService;
import org.edu.service.StaffService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.edu.util.EmploymentType;
import org.edu.util.Role;
import org.edu.util.StaffCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Staff", description = "Manage staff records and staff directory queries")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {
    private final StaffService staffService;
    private final AuditLogService auditLogService;


    @PostMapping
    @Operation(summary = "Create a staff record")
    public StaffDTO createStaff(@Valid @RequestBody StaffDTO staffDTO) {
        StaffDTO response = staffService.createStaff(staffDTO);
        auditLogService.log(AuditAction.CREATE, AuditEntityType.STAFF, response.getId(), response.getName(), "Created staff record");
        return response;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a staff record")
    public StaffDTO updateStaff(@PathVariable Long id,
                                    @Valid @RequestBody StaffDTO staffDTO) {
        StaffDTO response = staffService.updateStaff(id, staffDTO);
        auditLogService.log(AuditAction.UPDATE, AuditEntityType.STAFF, response.getId(), response.getName(), "Updated staff record");
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a staff record")
    public void deleteStaff(@PathVariable Long id) {
        String staffName = staffService.getStaffById(id).getName();
        staffService.deleteStaff(id);
        auditLogService.log(AuditAction.DEACTIVATE, AuditEntityType.STAFF, id, staffName, "Deactivated staff record");
    }

    @GetMapping
    @Operation(summary = "List staff records")
    public Page<StaffDTO> getAllStaff(Pageable pageable) {
        return staffService.getAllStaff(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search staff by name")
    public Page<StaffDTO> searchStaff(@RequestParam String name,
                                           Pageable pageable) {
        return staffService.searchStaff(name, pageable);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter staff by directory, role, category, employment, department, teaching capability, and status")
    public Page<StaffDTO> filterStaff(@RequestParam Map<String, String> filters, Pageable pageable) {
        return staffService.filterStaff(filters, pageable);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "List active staff members")
    public List<StaffDTO> getActiveStaff() {
        return staffService.getAllActiveStaff();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get a staff record by id")
    public StaffDTO getStaffById(@PathVariable Long id) {
        return staffService.getStaffById(id);
    }
}
