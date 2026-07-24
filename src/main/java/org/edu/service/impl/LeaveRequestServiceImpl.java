package org.edu.service.impl;

import java.time.LocalDateTime;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.parentportal.ParentPortalLeaveRequestCreateDTO;
import org.edu.dto.request.LeaveRequestReviewRequest;
import org.edu.entity.LeaveRequest;
import org.edu.entity.Parent;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.LeaveRequestMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.LeaveRequestRepository;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.AttendanceService;
import org.edu.service.LeaveRequestService;
import org.edu.util.AttendanceStatus;
import org.edu.util.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final StaffRepository staffRepository;
    private final ClassRepository classRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceService attendanceService;
    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto) {
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        Parent parent = getActiveParent(dto.getParentId());
        Student student = getActiveStudent(dto.getStudentId());
        validateParentStudentLink(parent.getId(), student.getId());
        validateOverlappingRequests(student.getId(), dto.getStartDate(), dto.getEndDate(), null);

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(dto);
        leaveRequest.setParent(parent);
        leaveRequest.setStudent(student);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        return leaveRequestMapper.toDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public LeaveRequestDTO updateLeaveRequest(Long id, LeaveRequestDTO dto) {
        LeaveRequest leaveRequest = getLeaveRequest(id);
        ensurePending(leaveRequest, "Only pending leave requests can be updated");

        Long parentId = dto.getParentId() == null ? leaveRequest.getParent().getId() : dto.getParentId();
        Long studentId = dto.getStudentId() == null ? leaveRequest.getStudent().getId() : dto.getStudentId();

        Parent parent = getActiveParent(parentId);
        Student student = getActiveStudent(studentId);
        validateParentStudentLink(parent.getId(), student.getId());

        LeaveRequestDTO resolved = resolveForUpdate(dto, leaveRequest, parentId, studentId);
        validateDateRange(resolved.getStartDate(), resolved.getEndDate());
        validateOverlappingRequests(student.getId(), resolved.getStartDate(), resolved.getEndDate(), id);

        leaveRequestMapper.updateEntityFromDTO(dto, leaveRequest);
        leaveRequest.setParent(parent);
        leaveRequest.setStudent(student);

        return leaveRequestMapper.toDTO(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public LeaveRequestDTO approveLeaveRequest(Long id, LeaveRequestReviewRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(id);
        ensurePending(leaveRequest, "Only pending leave requests can be approved");
        applyReview(leaveRequest, request, LeaveRequestStatus.APPROVED);
        return leaveRequestMapper.toDTO(leaveRequest);
    }

    @Override
    public LeaveRequestDTO rejectLeaveRequest(Long id, LeaveRequestReviewRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(id);
        ensurePending(leaveRequest, "Only pending leave requests can be rejected");
        applyReview(leaveRequest, request, LeaveRequestStatus.REJECTED);
        return leaveRequestMapper.toDTO(leaveRequest);
    }

    @Override
    public LeaveRequestDTO cancelLeaveRequest(Long id, String reviewerRemarks) {
        LeaveRequest leaveRequest = getLeaveRequest(id);
        ensurePending(leaveRequest, "Only pending leave requests can be cancelled");
        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequest.setReviewerRemarks(reviewerRemarks);
        leaveRequest.setReviewedBy(null);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        return leaveRequestMapper.toDTO(leaveRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestDTO getLeaveRequestById(Long id) {
        return leaveRequestMapper.toDTO(getLeaveRequest(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getAllLeaveRequests(Pageable pageable) {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(leaveRequestMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByStatus(LeaveRequestStatus status, Pageable pageable) {
        return leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(leaveRequestMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByStudent(Long studentId, Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(studentId, pageable)
                .map(leaveRequestMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByParent(Long parentId, Pageable pageable) {
        if (!parentRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent not found with id: " + parentId);
        }
        return leaveRequestRepository.findByParentIdOrderByCreatedAtDesc(parentId, pageable)
                .map(leaveRequestMapper::toDTO);
    }

    @Override
    public LeaveRequestDTO createParentLeaveRequest(Long authenticatedUserId, ParentPortalLeaveRequestCreateDTO dto) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);

        LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();
        leaveRequestDTO.setParentId(parent.getId());
        leaveRequestDTO.setStudentId(dto.getStudentId());
        leaveRequestDTO.setStartDate(dto.getStartDate());
        leaveRequestDTO.setEndDate(dto.getEndDate());
        leaveRequestDTO.setReason(dto.getReason());
        leaveRequestDTO.setNote(dto.getNote());

        return createLeaveRequest(leaveRequestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getParentLeaveRequests(Long authenticatedUserId, Pageable pageable) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        return leaveRequestRepository.findByParentIdOrderByCreatedAtDesc(parent.getId(), pageable)
                .map(leaveRequestMapper::toDTO);
    }

    @Override
    public LeaveRequestDTO cancelParentLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String remarks) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        LeaveRequest leaveRequest = getLeaveRequest(leaveRequestId);

        if (!leaveRequest.getParent().getId().equals(parent.getId())) {
            throw new ResourceNotFoundException("Leave request not found in current parent portal");
        }

        return cancelLeaveRequest(leaveRequestId, remarks);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getTeacherLeaveRequests(Long authenticatedUserId, LeaveRequestStatus status, Pageable pageable) {
        Staff staff = getActiveStaffByUserId(authenticatedUserId);
        Page<LeaveRequest> page = status == null
                ? leaveRequestRepository.findTeacherPortalLeaveRequestsByStaffId(staff.getId(), pageable)
                : leaveRequestRepository.findTeacherPortalLeaveRequestsByStaffIdAndStatus(staff.getId(), status, pageable);
        return page.map(leaveRequestMapper::toDTO);
    }

    @Override
    public LeaveRequestDTO approveTeacherLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String reviewerRemarks) {
        Staff staff = getActiveStaffByUserId(authenticatedUserId);
        LeaveRequest leaveRequest = getTeacherAccessibleLeaveRequest(staff.getId(), leaveRequestId);
        return approveLeaveRequest(
                leaveRequest.getId(),
                new LeaveRequestReviewRequest(staff.getId(), reviewerRemarks)
        );
    }

    @Override
    public LeaveRequestDTO rejectTeacherLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String reviewerRemarks) {
        Staff staff = getActiveStaffByUserId(authenticatedUserId);
        LeaveRequest leaveRequest = getTeacherAccessibleLeaveRequest(staff.getId(), leaveRequestId);
        return rejectLeaveRequest(
                leaveRequest.getId(),
                new LeaveRequestReviewRequest(staff.getId(), reviewerRemarks)
        );
    }

    @Override
    public LeaveRequestDTO applyApprovedLeaveToAttendance(Long authenticatedUserId, Long leaveRequestId) {
        Staff staff = getActiveStaffByUserId(authenticatedUserId);
        LeaveRequest leaveRequest = getTeacherAccessibleLeaveRequest(staff.getId(), leaveRequestId);

        if (leaveRequest.getStatus() != LeaveRequestStatus.APPROVED) {
            throw new IllegalStateException("Only approved leave requests can be applied to attendance");
        }
        if (leaveRequest.isAttendanceApplied()) {
            throw new IllegalStateException("Attendance has already been applied for this leave request");
        }

        java.time.LocalDate current = leaveRequest.getStartDate();
        while (!current.isAfter(leaveRequest.getEndDate())) {
            AttendanceDTO attendanceDTO = new AttendanceDTO();
            attendanceDTO.setStudentId(leaveRequest.getStudent().getId());
            attendanceDTO.setAttendanceDate(current);
            attendanceDTO.setStatus(AttendanceStatus.EXCUSED);
            attendanceDTO.setMarkedByStaffId(staff.getId());
            attendanceDTO.setRemarks("Applied from approved leave request #" + leaveRequest.getId());
            attendanceService.createAttendance(null, attendanceDTO);
            current = current.plusDays(1);
        }

        leaveRequest.setAttendanceApplied(true);
        leaveRequest.setAttendanceAppliedAt(LocalDateTime.now());
        return leaveRequestMapper.toDTO(leaveRequest);
    }

    private LeaveRequest getLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
    }

    private Parent getActiveParent(Long parentId) {
        return parentRepository.findByIdAndActiveTrue(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active parent not found with id: " + parentId));
    }

    private Parent getActiveParentByUserId(Long authenticatedUserId) {
        return parentRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active parent profile not found for current user"));
    }

    private Staff getActiveStaffByUserId(Long authenticatedUserId) {
        return staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"));
    }

    private Student getActiveStudent(Long studentId) {
        return studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active student not found with id: " + studentId));
    }

    private void validateParentStudentLink(Long parentId, Long studentId) {
        if (!parentStudentRepository.existsByParentIdAndStudentId(parentId, studentId)) {
            throw new IllegalArgumentException("Selected parent is not linked to the selected student");
        }
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    private void validateOverlappingRequests(Long studentId, java.time.LocalDate startDate, java.time.LocalDate endDate, Long currentId) {
        boolean exists = currentId == null
                ? leaveRequestRepository.existsOverlappingRequest(
                        studentId,
                        startDate,
                        endDate,
                        EnumSet.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)
                )
                : leaveRequestRepository.existsOverlappingRequestExcludingId(
                        studentId,
                        startDate,
                        endDate,
                        EnumSet.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED),
                        currentId
                );

        if (exists) {
            throw new IllegalStateException("An overlapping leave request already exists for this student");
        }
    }

    private void ensurePending(LeaveRequest leaveRequest, String message) {
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException(message);
        }
    }

    private void applyReview(LeaveRequest leaveRequest, LeaveRequestReviewRequest request, LeaveRequestStatus status) {
        leaveRequest.setReviewedBy(staffRepository.findByIdAndActiveTrue(request.getReviewedByStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Active staff not found with id: " + request.getReviewedByStaffId())));
        leaveRequest.setReviewerRemarks(request.getReviewerRemarks());
        leaveRequest.setReviewedAt(LocalDateTime.now());
        leaveRequest.setStatus(status);
    }

    private LeaveRequest getTeacherAccessibleLeaveRequest(Long staffId, Long leaveRequestId) {
        LeaveRequest leaveRequest = getLeaveRequest(leaveRequestId);
        if (leaveRequest.getStudent().getCurrentClass() == null) {
            throw new ResourceNotFoundException("Leave request not found in current teacher portal");
        }

        Long classId = leaveRequest.getStudent().getCurrentClass().getId();
        boolean classTeacherAccess = classRepository.existsByIdAndClassTeacherIdAndActiveTrue(classId, staffId);
        boolean teachingAccess = timetableRepository.existsByStaffIdAndStudentClassId(staffId, classId);

        if (!classTeacherAccess && !teachingAccess) {
            throw new ResourceNotFoundException("Leave request not found in current teacher portal");
        }

        return leaveRequest;
    }

    private LeaveRequestDTO resolveForUpdate(LeaveRequestDTO dto, LeaveRequest leaveRequest, Long parentId, Long studentId) {
        LeaveRequestDTO resolved = new LeaveRequestDTO();
        resolved.setParentId(parentId);
        resolved.setStudentId(studentId);
        resolved.setStartDate(dto.getStartDate() == null ? leaveRequest.getStartDate() : dto.getStartDate());
        resolved.setEndDate(dto.getEndDate() == null ? leaveRequest.getEndDate() : dto.getEndDate());
        resolved.setReason(dto.getReason() == null ? leaveRequest.getReason() : dto.getReason());
        resolved.setNote(dto.getNote() == null ? leaveRequest.getNote() : dto.getNote());
        return resolved;
    }
}
