package org.edu.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.edu.dto.StaffDTO;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StaffMapper;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StaffRepository;
import org.edu.repository.UserRepository;
import org.edu.service.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StafffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final UserRepository userRepository;

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {

        User user = userRepository.findById(staffDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (staffRepository.existsByUser(user)) {
            throw new IllegalArgumentException("User already assigned to a teacher");
        }

        if (!user.getRole().name().equals("TEACHER")) {
            throw new IllegalArgumentException("User must have TEACHER role");
        }

        Staff staff = staffMapper.toEntity(staffDTO);
        staff.setUser(user);
        staff.setActive(true);

        Staff updated = staffRepository.save(staff);
        return staffMapper.toDTO(updated);


    }

    @Override
    public StaffDTO updateStaff(Long id, StaffDTO staffDTO) {

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        staffMapper.updateEntityFromDTO(staffDTO, staff);

        Staff updated = staffRepository.save(staff);

        return staffMapper.toDTO(updated);
    }

    @Override
    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!staff.isActive()) {
            throw new IllegalStateException("Student already inactive");
        }

        staff.setActive(false);

    }

    @Override
    public Page<StaffDTO> getAllStaff(Pageable pageable) {
        return staffRepository.findByActiveTrue(pageable)
                .map(staffMapper::toDTO);
    }

    @Override
    public StaffDTO getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        return staffMapper.toDTO(staff);
    }

    @Override
    public Page<StaffDTO> searchStaff(String name, Pageable pageable) {
        return staffRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(staffMapper::toDTO);
    }

    @Override
    public List<StaffDTO> getAllActiveStaff() {
        return staffRepository.findByActiveTrue()
                .stream()
                .map(staffMapper::toDTO)
                .toList();
    }
}
