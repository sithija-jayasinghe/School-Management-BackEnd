package org.edu.mapper;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StaffDTO;
import org.edu.entity.Staff;
import org.edu.repository.StaffRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffMapper {

    public Staff toEntity(StaffDTO dto) {
        if (dto == null) return null;

        Staff staff = new Staff();
        staff.setStaffId(dto.getStaffId());
        staff.setName(dto.getName());
        staff.setActive(dto.isActive());
        staff.setPhoneNumber(dto.getPhoneNumber());
        staff.setDesignation(dto.getDesignation());

        return staff;
    }

    public StaffDTO toDTO(Staff staff) {
        if (staff == null) return null;

        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setStaffId(staff.getStaffId());
        dto.setName(staff.getName());
        dto.setActive(staff.isActive());
        dto.setPhoneNumber(staff.getPhoneNumber());
        dto.setDesignation(staff.getDesignation());
        dto.setCreatedAt(staff.getCreatedAt());
        dto.setUpdatedAt(staff.getUpdatedAt());

        if (staff.getUser() != null) {
            dto.setUserId(staff.getUser().getId());
        }

        return dto;
    }

    public void updateEntityFromDTO(StaffDTO dto, Staff staff) {
        if (dto == null || staff == null) return;

        if (dto.getName() != null) {
            staff.setName(dto.getName());
        }

        if (dto.getPhoneNumber() != null) {
            staff.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getDesignation() != null) {
            staff.setDesignation(dto.getDesignation());
        }
    }
}
