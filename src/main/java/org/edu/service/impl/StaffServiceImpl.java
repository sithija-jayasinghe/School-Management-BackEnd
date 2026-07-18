package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StaffDTO;
import org.edu.entity.Staff;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StaffMapper;
import org.edu.repository.StaffRepository;
import org.edu.repository.UserRepository;
import org.edu.service.StaffService;
import org.edu.util.Role;
import org.edu.util.EmploymentType;
import org.edu.util.StaffCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final UserRepository userRepository;

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {

        User user = resolveOptionalUser(staffDTO.getUserId());

        Staff staff = staffMapper.toEntity(staffDTO);
        staff.setUser(user);
        staff.setActive(true);
        applyStaffDefaults(staff, user);

        Staff updated = staffRepository.save(staff);
        return staffMapper.toDTO(updated);


    }

    @Override
    public StaffDTO updateStaff(Long id, StaffDTO staffDTO) {

        Staff staff = staffRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        staffMapper.updateEntityFromDTO(staffDTO, staff);

        Staff updated = staffRepository.save(staff);

        return staffMapper.toDTO(updated);
    }

    @Override
    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        if (!staff.isActive()) {
            throw new IllegalStateException("Staff already inactive");
        }

        staff.setActive(false);
        staffRepository.save(staff);
    }

    @Override
    public Page<StaffDTO> getAllStaff(Pageable pageable) {
        return staffRepository.findByActiveTrue(pageable)
                .map(staffMapper::toDTO);
    }

    @Override
    public StaffDTO getStaffById(Long id) {
        Staff staff = staffRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        return staffMapper.toDTO(staff);
    }

    @Override
    public Page<StaffDTO> searchStaff(String name, Pageable pageable) {
        return staffRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(staffMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffDTO> filterStaff(String keyword, StaffCategory category, EmploymentType employmentType, String department, Boolean teachingCapable, Role role, Boolean active, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        String normalizedDepartment = department == null || department.trim().isEmpty() ? null : department.trim();
        return staffRepository
                .filterStaff(normalizedKeyword, category, employmentType, normalizedDepartment, teachingCapable, role, active, pageable)
                .map(staffMapper::toDTO);
    }

    @Override
    public List<StaffDTO> getAllActiveStaff() {
        return staffRepository.findByActiveTrue()
                .stream()
                .map(staffMapper::toDTO)
                .toList();
    }

    private User resolveOptionalUser(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (staffRepository.existsByUser(user)) {
            throw new IllegalArgumentException("User is already linked to a staff record");
        }
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.STAFF && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Staff accounts must have ADMIN, TEACHER, or STAFF role");
        }
        return user;
    }

    private void applyStaffDefaults(Staff staff, User user) {
        if (staff.getStaffCategory() == null) {
            staff.setStaffCategory(user != null && user.getRole() == Role.TEACHER
                    ? StaffCategory.ACADEMIC
                    : StaffCategory.SUPPORT);
        }
        if (staff.getEmploymentType() == null) {
            staff.setEmploymentType(EmploymentType.PERMANENT);
        }
        if (staff.getTeachingCapable() == null) {
            staff.setTeachingCapable(user != null && user.getRole() == Role.TEACHER);
        }
    }
}
