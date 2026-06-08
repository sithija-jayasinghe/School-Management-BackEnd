package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.request.LeaveRequestReviewRequest;
import org.edu.entity.LeaveRequest;
import org.edu.entity.Parent;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.mapper.LeaveRequestMapper;
import org.edu.repository.LeaveRequestRepository;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.util.LeaveRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ParentStudentRepository parentStudentRepository;

    @Mock
    private StaffRepository staffRepository;

    private LeaveRequestServiceImpl leaveRequestService;

    @BeforeEach
    void setUp() {
        LeaveRequestMapper leaveRequestMapper = Mappers.getMapper(LeaveRequestMapper.class);
        leaveRequestService = new LeaveRequestServiceImpl(
                leaveRequestRepository,
                parentRepository,
                studentRepository,
                parentStudentRepository,
                staffRepository,
                leaveRequestMapper
        );
    }

    @Test
    void shouldCreatePendingLeaveRequestForLinkedParentStudent() {
        LeaveRequestDTO dto = leaveRequestRequest();
        Parent parent = activeParent(10L);
        Student student = activeStudent(20L, 30L);

        when(parentRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(parent));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(parentStudentRepository.existsByParentIdAndStudentId(10L, 20L)).thenReturn(true);
        when(leaveRequestRepository.existsOverlappingRequest(
                20L,
                dto.getStartDate(),
                dto.getEndDate(),
                java.util.EnumSet.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)
        )).thenReturn(false);
        when(leaveRequestRepository.save(org.mockito.Mockito.any(LeaveRequest.class)))
                .thenAnswer(invocation -> {
                    LeaveRequest leaveRequest = invocation.getArgument(0);
                    leaveRequest.setId(1L);
                    return leaveRequest;
                });

        LeaveRequestDTO saved = leaveRequestService.createLeaveRequest(dto);

        assertEquals(1L, saved.getId());
        assertEquals(LeaveRequestStatus.PENDING, saved.getStatus());
        assertEquals("Parent User", saved.getParentName());
        assertEquals("Student User", saved.getStudentName());
    }

    @Test
    void shouldRejectLeaveRequestForUnlinkedParentStudent() {
        LeaveRequestDTO dto = leaveRequestRequest();

        when(parentRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(activeParent(10L)));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(activeStudent(20L, 30L)));
        when(parentStudentRepository.existsByParentIdAndStudentId(10L, 20L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> leaveRequestService.createLeaveRequest(dto));
        verify(leaveRequestRepository, never()).save(org.mockito.Mockito.any(LeaveRequest.class));
    }

    @Test
    void shouldRejectOverlappingPendingOrApprovedRequest() {
        LeaveRequestDTO dto = leaveRequestRequest();

        when(parentRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(activeParent(10L)));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(activeStudent(20L, 30L)));
        when(parentStudentRepository.existsByParentIdAndStudentId(10L, 20L)).thenReturn(true);
        when(leaveRequestRepository.existsOverlappingRequest(
                20L,
                dto.getStartDate(),
                dto.getEndDate(),
                java.util.EnumSet.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> leaveRequestService.createLeaveRequest(dto));
    }

    @Test
    void shouldApprovePendingLeaveRequest() {
        LeaveRequest leaveRequest = pendingLeaveRequest();
        Staff staff = activeStaff(50L);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(staffRepository.findByIdAndActiveTrue(50L)).thenReturn(Optional.of(staff));

        LeaveRequestDTO approved = leaveRequestService.approveLeaveRequest(
                1L,
                new LeaveRequestReviewRequest(50L, "Approved for medical reason")
        );

        assertEquals(LeaveRequestStatus.APPROVED, approved.getStatus());
        assertEquals(50L, approved.getReviewedByStaffId());
        assertNotNull(approved.getReviewedAt());
    }

    @Test
    void shouldRejectUpdatingNonPendingLeaveRequest() {
        LeaveRequest leaveRequest = pendingLeaveRequest();
        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));

        assertThrows(IllegalStateException.class,
                () -> leaveRequestService.updateLeaveRequest(1L, new LeaveRequestDTO()));
    }

    private LeaveRequestDTO leaveRequestRequest() {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setParentId(10L);
        dto.setStudentId(20L);
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 12));
        dto.setReason("Medical appointment");
        dto.setNote("Clinic visit");
        return dto;
    }

    private Parent activeParent(Long parentId) {
        Parent parent = new Parent();
        parent.setId(parentId);
        parent.setName("Parent User");
        parent.setActive(true);
        return parent;
    }

    private Student activeStudent(Long studentId, Long classId) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName("Grade 10A");

        Student student = new Student();
        student.setId(studentId);
        student.setName("Student User");
        student.setCurrentClass(studentClass);
        student.setActive(true);
        return student;
    }

    private Staff activeStaff(Long staffId) {
        Staff staff = new Staff();
        staff.setId(staffId);
        staff.setName("Nimal Perera");
        staff.setActive(true);
        return staff;
    }

    private LeaveRequest pendingLeaveRequest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setParent(activeParent(10L));
        leaveRequest.setStudent(activeStudent(20L, 30L));
        leaveRequest.setStartDate(LocalDate.of(2026, 6, 10));
        leaveRequest.setEndDate(LocalDate.of(2026, 6, 12));
        leaveRequest.setReason("Medical appointment");
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        return leaveRequest;
    }
}
