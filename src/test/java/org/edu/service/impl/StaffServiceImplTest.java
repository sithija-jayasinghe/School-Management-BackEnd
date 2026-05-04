package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.edu.entity.Staff;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StaffMapper;
import org.edu.repository.StaffRepository;
import org.edu.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    }

    @Test
    void shouldRejectMissingStaffDelete() {
        when(staffRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffService.deleteStaff(99L));
    }
}
