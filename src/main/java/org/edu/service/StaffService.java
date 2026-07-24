package org.edu.service;

import org.edu.dto.StaffDTO;
import org.edu.util.EmploymentType;
import org.edu.util.Role;
import org.edu.util.StaffCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {
    StaffDTO createStaff(StaffDTO staffDTO);
    StaffDTO updateStaff(Long id,StaffDTO staffDTO);
    void deleteStaff(Long id); //For Dev : Soft delete (sets active = false)
    Page<StaffDTO> getAllStaff(Pageable pageable);
    StaffDTO getStaffById(Long id);
    Page<StaffDTO> searchStaff(String name, Pageable pageable);
    Page<StaffDTO> filterStaff(String keyword, StaffCategory category, EmploymentType employmentType, String department, Boolean teachingCapable, Role role, Boolean active, Pageable pageable);
    List<StaffDTO> getAllActiveStaff();
}
