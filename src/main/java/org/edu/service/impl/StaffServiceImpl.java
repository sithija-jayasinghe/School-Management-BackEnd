package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StaffDTO;
import org.edu.entity.Staff;
import org.edu.entity.User;
import org.edu.exception.DuplicateEmailException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.filter.FilterSpecifications;
import org.edu.filter.StaffFilterDefinitions;
import org.edu.mapper.StaffMapper;
import org.edu.repository.StaffRepository;
import org.edu.repository.UserRepository;
import org.edu.service.StaffService;
import org.edu.util.Role;
import org.edu.util.EmploymentType;
import org.edu.util.StaffCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {

        User user = resolveUserForStaff(staffDTO);

        Staff staff = staffMapper.toEntity(staffDTO);
        staff.setNic(normalizeNic(staffDTO.getNic()));
        staff.setUser(user);
        staff.setActive(true);
        applyStaffDefaults(staff, user);

        Staff updated = staffRepository.save(staff);
        return toDTOWithNic(updated);


    }

    @Override
    public StaffDTO updateStaff(Long id, StaffDTO staffDTO) {

        Staff staff = staffRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        staffMapper.updateEntityFromDTO(staffDTO, staff);
        if (staffDTO.getNic() != null) {
            staff.setNic(normalizeNic(staffDTO.getNic()));
        }

        Staff updated = staffRepository.save(staff);

        return toDTOWithNic(updated);
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
                .map(this::toDTOWithNic);
    }

    @Override
    public StaffDTO getStaffById(Long id) {
        Staff staff = staffRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        return toDTOWithNic(staff);
    }

    @Override
    public Page<StaffDTO> searchStaff(String name, Pageable pageable) {
        return staffRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(this::toDTOWithNic);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffDTO> filterStaff(Map<String, String> filters, Pageable pageable) {
        return staffRepository
                .findAll(FilterSpecifications.build(filters, StaffFilterDefinitions.definitions()), pageable)
                .map(this::toDTOWithNic);
    }

    @Override
    public List<StaffDTO> getAllActiveStaff() {
        return staffRepository.findByActiveTrue()
                .stream()
                .map(this::toDTOWithNic)
                .toList();
    }

    private StaffDTO toDTOWithNic(Staff staff) {
        StaffDTO dto = staffMapper.toDTO(staff);
        dto.setNic(staff.getNic());
        return dto;
    }

    private User resolveUserForStaff(StaffDTO staffDTO) {
        boolean hasInlineLogin = hasText(staffDTO.getLoginEmail()) || hasText(staffDTO.getLoginPassword());
        if (staffDTO.getUserId() != null && hasInlineLogin) {
            throw new IllegalArgumentException("Provide either an existing userId or new login details, not both");
        }
        if (staffDTO.getUserId() != null) {
            return resolveOptionalUser(staffDTO.getUserId());
        }
        if (hasInlineLogin) {
            Role role = staffDTO.getLoginRole() != null
                    ? staffDTO.getLoginRole()
                    : (Boolean.TRUE.equals(staffDTO.getTeachingCapable()) ? Role.TEACHER : Role.STAFF);
            if (role != Role.ADMIN && role != Role.TEACHER && role != Role.STAFF) {
                throw new IllegalArgumentException("Staff accounts must have ADMIN, TEACHER, or STAFF role");
            }
            return createLinkedUser(staffDTO.getName(), staffDTO.getLoginEmail(), staffDTO.getLoginPassword(), role);
        }
        return null;
    }

    private User resolveOptionalUser(Long userId) {
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

    private User createLinkedUser(String name, String email, String password, Role role) {
        if (!hasText(email) || !hasText(password)) {
            throw new IllegalArgumentException("Login email and password are both required to create a login");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeNic(String nic) {
        if (!hasText(nic)) {
            return null;
        }
        return nic.trim().toUpperCase();
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
