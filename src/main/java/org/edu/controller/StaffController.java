package org.edu.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.StaffDTO;
import org.edu.service.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Manage staff records and staff directory queries")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {
    private final StaffService staffService;


    @PostMapping
    @Operation(summary = "Create a staff record")
    public StaffDTO createStaff(@Valid @RequestBody StaffDTO staffDTO) {
        return staffService.createStaff(staffDTO);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a staff record")
    public StaffDTO updateStaff(@PathVariable Long id,
                                    @RequestBody StaffDTO staffDTO) {
        return staffService.updateStaff(id, staffDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a staff record")
    public void deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
    }

    @GetMapping
    @Operation(summary = "List staff records")
    public Page<StaffDTO> getAllStaff(Pageable pageable) {
        return staffService.getAllStaff(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a staff record by id")
    public StaffDTO getStaffById(@PathVariable Long id) {
        return staffService.getStaffById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search staff by name")
    public Page<StaffDTO> searchStaff(@RequestParam String name,
                                           Pageable pageable) {
        return staffService.searchStaff(name, pageable);
    }

    @GetMapping("/active")
    @Operation(summary = "List active staff members")
    public List<StaffDTO> getActiveStaff() {
        return staffService.getAllActiveStaff();
    }
}
