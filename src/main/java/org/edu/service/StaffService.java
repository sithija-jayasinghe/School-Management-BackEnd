package org.edu.service;

import org.edu.dto.StaffDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface StaffService {
    StaffDTO createStaff(StaffDTO staffDTO);
    StaffDTO updateStaff(Long id,StaffDTO staffDTO);
    void deleteStaff(Long id); //For Dev : Soft delete (sets active = false)
    Page<StaffDTO> getAllStaff(Pageable pageable);
    StaffDTO getStaffById(Long id);
    Page<StaffDTO> searchStaff(String name, Pageable pageable);
    Page<StaffDTO> filterStaff(Map<String, String> filters, Pageable pageable);
    List<StaffDTO> getAllActiveStaff();
}
