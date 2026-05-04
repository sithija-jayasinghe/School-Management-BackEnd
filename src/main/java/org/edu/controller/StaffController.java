package org.edu.controller;


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
public class StaffController {
    private final StaffService staffService;


    @PostMapping
    public StaffDTO createStaff(@Valid @RequestBody StaffDTO staffDTO) {
        return staffService.createStaff(staffDTO);
    }

    @PatchMapping("/{id}")
    public StaffDTO updateStaff(@PathVariable Long id,
                                    @RequestBody StaffDTO staffDTO) {
        return staffService.updateStaff(id, staffDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
    }

    @GetMapping
    public Page<StaffDTO> getAllStaff(Pageable pageable) {
        return staffService.getAllStaff(pageable);
    }

    @GetMapping("/{id}")
    public StaffDTO getStaffById(@PathVariable Long id) {
        return staffService.getStaffById(id);
    }

    @GetMapping("/search")
    public Page<StaffDTO> searchStaff(@RequestParam String name,
                                           Pageable pageable) {
        return staffService.searchStaff(name, pageable);
    }

    @GetMapping("/active")
    public List<StaffDTO> getActiveStaff() {
        return staffService.getAllActiveStaff();
    }
}
