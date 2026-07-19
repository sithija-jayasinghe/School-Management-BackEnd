package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.edu.entity.Staff;
import org.edu.entity.User;
import org.edu.dto.StaffDTO;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StaffMapper;
import org.edu.repository.StaffRepository;
import org.edu.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.edu.util.EmploymentType;
import org.edu.util.Role;
import org.edu.util.StaffCategory;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffMapper staffMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StaffServiceImpl staffService;

    @Test
    void shouldSoftDeleteActiveStaff() {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setActive(true);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));

        staffService.deleteStaff(1L);

        assertFalse(staff.isActive());
        verify(staffRepository).save(staff);
    }

    @Test
    void shouldRejectMissingStaffDelete() {
        when(staffRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffService.deleteStaff(99L));
    }

    @Test
    void shouldCreateSupportStaffWithoutSystemAccount() {
        StaffDTO input = staffInput();
        input.setUserId(null);
        input.setStaffCategory(StaffCategory.SUPPORT);
        input.setEmploymentType(EmploymentType.CONTRACT);
        input.setTeachingCapable(false);
        Staff mapped = new Staff();
        mapped.setStaffCategory(input.getStaffCategory());
        mapped.setEmploymentType(input.getEmploymentType());
        mapped.setTeachingCapable(input.getTeachingCapable());

        when(staffMapper.toEntity(input)).thenReturn(mapped);
        when(staffRepository.save(mapped)).thenReturn(mapped);
        when(staffMapper.toDTO(mapped)).thenReturn(input);

        staffService.createStaff(input);

        assertNull(mapped.getUser());
        assertEquals(StaffCategory.SUPPORT, mapped.getStaffCategory());
        assertFalse(mapped.getTeachingCapable());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldApplyTeacherDefaultsForLegacyTeacherAccount() {
        StaffDTO input = staffInput();
        input.setUserId(5L);
        User teacherUser = new User();
        teacherUser.setId(5L);
        teacherUser.setRole(Role.TEACHER);
        Staff mapped = new Staff();

        when(userRepository.findById(5L)).thenReturn(Optional.of(teacherUser));
        when(staffRepository.existsByUser(teacherUser)).thenReturn(false);
        when(staffMapper.toEntity(input)).thenReturn(mapped);
        when(staffRepository.save(mapped)).thenReturn(mapped);
        when(staffMapper.toDTO(mapped)).thenReturn(input);

        staffService.createStaff(input);

        assertEquals(StaffCategory.ACADEMIC, mapped.getStaffCategory());
        assertEquals(EmploymentType.PERMANENT, mapped.getEmploymentType());
        assertEquals(true, mapped.getTeachingCapable());
    }

    @Test
    void shouldRejectStudentAccountForStaffRecord() {
        StaffDTO input = staffInput();
        input.setUserId(8L);
        User studentUser = new User();
        studentUser.setId(8L);
        studentUser.setRole(Role.STUDENT);
        when(userRepository.findById(8L)).thenReturn(Optional.of(studentUser));

        assertThrows(IllegalArgumentException.class, () -> staffService.createStaff(input));
        verify(staffRepository, never()).save(any());
    }

    private StaffDTO staffInput() {
        StaffDTO input = new StaffDTO();
        input.setStaffId("S001");
        input.setName("Nimali Perera");
        input.setPhoneNumber("0711234567");
        input.setDesignation("Library Assistant");
        return input;
    }
}
