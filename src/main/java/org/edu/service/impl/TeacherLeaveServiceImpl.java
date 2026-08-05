package org.edu.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.teacherleave.SubstituteTeacherOptionDTO;
import org.edu.dto.teacherleave.TeacherLeaveCoverageRequest;
import org.edu.dto.teacherleave.TeacherLeaveRequestDTO;
import org.edu.dto.teacherleave.TeacherLeaveReviewRequest;
import org.edu.dto.teacherleave.TeacherLeaveSaveRequest;
import org.edu.dto.teacherleave.TeacherLeaveSessionDTO;
import org.edu.entity.Staff;
import org.edu.entity.TeacherLeaveRequest;
import org.edu.entity.TeacherLeaveSession;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.StaffRepository;
import org.edu.repository.TeacherLeaveRequestRepository;
import org.edu.repository.TeacherLeaveSessionRepository;
import org.edu.repository.TeachingAssignmentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.AuditLogService;
import org.edu.service.TeacherLeaveService;
import org.edu.service.SchoolDayPolicyService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.edu.util.Role;
import org.edu.util.TeacherLeaveCoverageStatus;
import org.edu.util.TeacherLeaveDuration;
import org.edu.util.TeacherLeaveStatus;
import org.edu.util.TeacherLeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeacherLeaveServiceImpl implements TeacherLeaveService {

    private static final int MAX_LEAVE_DAYS = 30;
    private static final EnumSet<TeacherLeaveStatus> BLOCKING_STATUSES =
            EnumSet.of(TeacherLeaveStatus.PENDING, TeacherLeaveStatus.APPROVED);

    private final TeacherLeaveRequestRepository requestRepository;
    private final TeacherLeaveSessionRepository sessionRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AuditLogService auditLogService;
    private final SchoolDayPolicyService schoolDayPolicyService;

    @Override
    public TeacherLeaveRequestDTO submit(Long authenticatedUserId, TeacherLeaveSaveRequest input) {
        Staff teacher = getActiveStaffMember(authenticatedUserId);
        validateInput(input);
        validateNoOverlap(teacher.getId(), input, null);

        TeacherLeaveRequest request = new TeacherLeaveRequest();
        request.setTeacher(teacher);
        applyInput(request, input);
        request.setStatus(TeacherLeaveStatus.PENDING);
        request.replaceAffectedSessions(buildAffectedSessions(teacher, input));

        TeacherLeaveRequest saved = requestRepository.saveAndFlush(request);
        auditLogService.log(
                AuditAction.SUBMIT,
                AuditEntityType.TEACHER_LEAVE_REQUEST,
                saved.getId(),
                teacher.getName(),
                "Submitted teacher leave request for " + saved.getStartDate() + " to " + saved.getEndDate()
        );
        return toDto(saved);
    }

    @Override
    public TeacherLeaveRequestDTO updateOwnRequest(
            Long authenticatedUserId,
            Long requestId,
            TeacherLeaveSaveRequest input
    ) {
        Staff teacher = getActiveStaffMember(authenticatedUserId);
        TeacherLeaveRequest request = getOwnedRequest(teacher.getId(), requestId);
        ensurePending(request, "Only pending teacher leave requests can be updated");
        validateInput(input);
        validateNoOverlap(teacher.getId(), input, requestId);

        applyInput(request, input);
        request.replaceAffectedSessions(buildAffectedSessions(teacher, input));
        requestRepository.flush();
        auditLogService.log(
                AuditAction.UPDATE,
                AuditEntityType.TEACHER_LEAVE_REQUEST,
                request.getId(),
                teacher.getName(),
                "Updated pending teacher leave request"
        );
        return toDto(request);
    }

    @Override
    public TeacherLeaveRequestDTO cancelOwnRequest(Long authenticatedUserId, Long requestId) {
        Staff teacher = getActiveStaffMember(authenticatedUserId);
        TeacherLeaveRequest request = getOwnedRequest(teacher.getId(), requestId);
        ensurePending(request, "Only pending teacher leave requests can be cancelled");
        request.setStatus(TeacherLeaveStatus.CANCELLED);
        request.setReviewedAt(LocalDateTime.now());
        auditLogService.log(
                AuditAction.CANCEL,
                AuditEntityType.TEACHER_LEAVE_REQUEST,
                request.getId(),
                teacher.getName(),
                "Cancelled teacher leave request"
        );
        return toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherLeaveRequestDTO getOwnRequest(Long authenticatedUserId, Long requestId) {
        Staff teacher = getActiveStaffMember(authenticatedUserId);
        return toDto(getOwnedRequest(teacher.getId(), requestId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherLeaveRequestDTO> getOwnRequests(
            Long authenticatedUserId,
            TeacherLeaveStatus status,
            TeacherLeaveType leaveType,
            Pageable pageable
    ) {
        Staff teacher = getActiveStaffMember(authenticatedUserId);
        Page<TeacherLeaveRequest> requests;
        if (status != null && leaveType != null) {
            requests = requestRepository.findByTeacherIdAndStatusAndLeaveTypeOrderByCreatedAtDesc(
                    teacher.getId(),
                    status,
                    leaveType,
                    pageable
            );
        } else if (status != null) {
            requests = requestRepository.findByTeacherIdAndStatusOrderByCreatedAtDesc(teacher.getId(), status, pageable);
        } else if (leaveType != null) {
            requests = requestRepository.findByTeacherIdAndLeaveTypeOrderByCreatedAtDesc(teacher.getId(), leaveType, pageable);
        } else {
            requests = requestRepository.findByTeacherIdOrderByCreatedAtDesc(teacher.getId(), pageable);
        }
        return requests.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherLeaveRequestDTO> getAllRequests(TeacherLeaveStatus status, Pageable pageable) {
        Page<TeacherLeaveRequest> requests = status == null
                ? requestRepository.findAllByOrderByCreatedAtDesc(pageable)
                : requestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return requests.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherLeaveRequestDTO getRequest(Long requestId) {
        return toDto(getRequestEntity(requestId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherLeaveSessionDTO> getAffectedSessions(Long requestId) {
        getRequestEntity(requestId);
        return sessionRepository.findByLeaveRequestIdOrderBySessionDateAscStartTimeAsc(requestId)
                .stream()
                .map(this::toSessionDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubstituteTeacherOptionDTO> getSubstituteOptions(Long sessionId) {
        TeacherLeaveSession session = getSession(sessionId);
        return staffRepository.findByActiveTrue().stream()
                .filter(staff -> staff.getUser() != null && staff.getUser().getRole() == Role.TEACHER)
                .filter(staff -> !staff.getId().equals(session.getLeaveRequest().getTeacher().getId()))
                .filter(staff -> isAvailable(staff.getId(), session))
                .map(staff -> toSubstituteOption(staff, session))
                .sorted(Comparator
                        .comparing(SubstituteTeacherOptionDTO::isPreferredForSubject).reversed()
                        .thenComparing(
                                Comparator.comparing(SubstituteTeacherOptionDTO::isPreferredForClass).reversed()
                        )
                        .thenComparing(SubstituteTeacherOptionDTO::getName))
                .toList();
    }

    @Override
    public TeacherLeaveSessionDTO updateCoverage(Long sessionId, TeacherLeaveCoverageRequest input) {
        TeacherLeaveSession session = getSession(sessionId);
        ensurePending(session.getLeaveRequest(), "Coverage can only be changed while the leave request is pending");

        if (input.getCoverageStatus() == TeacherLeaveCoverageStatus.ASSIGNED) {
            if (input.getSubstituteStaffId() == null) {
                throw new IllegalArgumentException("A substitute teacher is required for assigned coverage");
            }
            Staff substitute = staffRepository.findByIdAndActiveTrue(input.getSubstituteStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active substitute teacher not found"));
            if (substitute.getUser() == null
                    || substitute.getUser().getRole() != Role.TEACHER
                    || !isAvailable(substitute.getId(), session)) {
                throw new IllegalStateException("Selected substitute teacher is not available for this session");
            }
            session.setSubstituteTeacher(substitute);
        } else if (input.getCoverageStatus() == TeacherLeaveCoverageStatus.NOT_REQUIRED) {
            if (input.getRemarks() == null || input.getRemarks().isBlank()) {
                throw new IllegalArgumentException("Remarks are required when coverage is not required");
            }
            session.setSubstituteTeacher(null);
        } else {
            session.setSubstituteTeacher(null);
        }

        session.setCoverageStatus(input.getCoverageStatus());
        session.setCoverageRemarks(trimToNull(input.getRemarks()));
        auditLogService.log(
                AuditAction.ASSIGN_COVERAGE,
                AuditEntityType.TEACHER_LEAVE_SESSION,
                session.getId(),
                session.getClassNameSnapshot() + " - " + session.getSubjectNameSnapshot(),
                "Set lesson coverage to " + session.getCoverageStatus()
        );
        return toSessionDto(session);
    }

    @Override
    public TeacherLeaveRequestDTO approve(
            Long authenticatedUserId,
            Long requestId,
            TeacherLeaveReviewRequest input
    ) {
        TeacherLeaveRequest request = getRequestEntity(requestId);
        ensurePending(request, "Only pending teacher leave requests can be approved");
        long uncovered = sessionRepository.countByLeaveRequestIdAndCoverageStatus(
                requestId,
                TeacherLeaveCoverageStatus.UNASSIGNED
        );
        if (uncovered > 0) {
            throw new IllegalStateException("Assign or explicitly waive coverage for every affected session before approval");
        }
        applyReview(request, authenticatedUserId, input, TeacherLeaveStatus.APPROVED);
        auditLogService.log(
                AuditAction.APPROVE,
                AuditEntityType.TEACHER_LEAVE_REQUEST,
                request.getId(),
                request.getTeacher().getName(),
                "Approved teacher leave request with " + request.getAffectedSessions().size() + " affected sessions"
        );
        return toDto(request);
    }

    @Override
    public TeacherLeaveRequestDTO reject(
            Long authenticatedUserId,
            Long requestId,
            TeacherLeaveReviewRequest input
    ) {
        TeacherLeaveRequest request = getRequestEntity(requestId);
        ensurePending(request, "Only pending teacher leave requests can be rejected");
        applyReview(request, authenticatedUserId, input, TeacherLeaveStatus.REJECTED);
        auditLogService.log(
                AuditAction.REJECT,
                AuditEntityType.TEACHER_LEAVE_REQUEST,
                request.getId(),
                request.getTeacher().getName(),
                "Rejected teacher leave request"
        );
        return toDto(request);
    }

    private void applyReview(
            TeacherLeaveRequest request,
            Long authenticatedUserId,
            TeacherLeaveReviewRequest input,
            TeacherLeaveStatus status
    ) {
        User reviewer = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewing user not found"));
        request.setStatus(status);
        request.setReviewedBy(reviewer);
        request.setReviewerRemarks(input.getReviewerRemarks().trim());
        request.setReviewedAt(LocalDateTime.now());
    }

    private List<TeacherLeaveSession> buildAffectedSessions(Staff teacher, TeacherLeaveSaveRequest input) {
        List<Timetable> schedule = timetableRepository.findTeacherPortalScheduleByStaffId(teacher.getId());
        return input.getStartDate().datesUntil(input.getEndDate().plusDays(1))
                .flatMap(date -> schedule.stream()
                        .filter(item -> item.getDayOfWeek() == date.getDayOfWeek())
                        .filter(item -> affectsSession(input, item))
                        .map(item -> toAffectedSession(date, item)))
                .toList();
    }

    private boolean affectsSession(TeacherLeaveSaveRequest input, Timetable timetable) {
        if (input.getDurationType() == TeacherLeaveDuration.FULL_DAY) {
            return true;
        }
        return timetable.getStartTime().isBefore(input.getEndTime())
                && timetable.getEndTime().isAfter(input.getStartTime());
    }

    private TeacherLeaveSession toAffectedSession(LocalDate date, Timetable timetable) {
        TeacherLeaveSession session = new TeacherLeaveSession();
        session.setTimetable(timetable);
        session.setSessionDate(date);
        session.setStartTime(timetable.getStartTime());
        session.setEndTime(timetable.getEndTime());
        session.setClassIdSnapshot(timetable.getStudentClass().getId());
        session.setClassNameSnapshot(timetable.getStudentClass().getName());
        session.setSubjectIdSnapshot(timetable.getSubject().getId());
        session.setSubjectNameSnapshot(timetable.getSubject().getName());
        session.setRoomNumberSnapshot(timetable.getRoomNumber());
        return session;
    }

    private boolean isAvailable(Long staffId, TeacherLeaveSession session) {
        if (staffId.equals(session.getLeaveRequest().getTeacher().getId())) {
            return false;
        }
        boolean timetableConflict = !timetableRepository.findTeacherConflicts(
                staffId,
                session.getSessionDate().getDayOfWeek(),
                session.getStartTime(),
                session.getEndTime()
        ).isEmpty();
        return !timetableConflict
                && !requestRepository.hasApprovedLeaveOnDate(staffId, session.getSessionDate())
                && !sessionRepository.hasSubstituteCoverageConflict(
                        staffId,
                        session.getSessionDate(),
                        session.getStartTime(),
                        session.getEndTime(),
                        session.getId()
                );
    }

    private SubstituteTeacherOptionDTO toSubstituteOption(Staff staff, TeacherLeaveSession session) {
        return new SubstituteTeacherOptionDTO(
                staff.getId(),
                staff.getStaffId(),
                staff.getName(),
                staff.getDesignation(),
                teachingAssignmentRepository.existsByStaffIdAndSubjectIdAndActiveTrue(
                        staff.getId(),
                        session.getSubjectIdSnapshot()
                ),
                teachingAssignmentRepository.existsByStaffIdAndStudentClassIdAndActiveTrue(
                        staff.getId(),
                        session.getClassIdSnapshot()
                )
        );
    }

    private void validateInput(TeacherLeaveSaveRequest input) {
        if (input.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Teacher leave can only be requested for today or a future date");
        }
        if (input.getDurationType() == TeacherLeaveDuration.PARTIAL_DAY) {
            if (!input.getStartDate().isEqual(input.getEndDate())) {
                throw new IllegalArgumentException("For part-of-a-day leave, start date and end date must be the same");
            }
        } else if (!input.getStartDate().isBefore(input.getEndDate())) {
            throw new IllegalArgumentException("End date must be after the start date");
        }
        long days = ChronoUnit.DAYS.between(input.getStartDate(), input.getEndDate()) + 1;
        if (days > MAX_LEAVE_DAYS) {
            throw new IllegalArgumentException("A teacher leave request cannot exceed " + MAX_LEAVE_DAYS + " days");
        }
        if (input.getDurationType() == TeacherLeaveDuration.PARTIAL_DAY) {
            if (input.getStartTime() == null || input.getEndTime() == null) {
                throw new IllegalArgumentException("Start time and end time are required for part-of-a-day leave");
            }
            schoolDayPolicyService.validateWithinSchoolDay(
                    input.getStartTime(),
                    input.getEndTime(),
                    "Partial-day teacher leave"
            );
        }
    }

    private void validateNoOverlap(Long teacherId, TeacherLeaveSaveRequest input, Long excludedId) {
        if (requestRepository.existsOverlappingRequest(
                teacherId,
                input.getStartDate(),
                input.getEndDate(),
                BLOCKING_STATUSES,
                excludedId
        )) {
            throw new IllegalStateException("A pending or approved teacher leave request already overlaps this period");
        }
    }

    private void applyInput(TeacherLeaveRequest request, TeacherLeaveSaveRequest input) {
        request.setLeaveType(input.getLeaveType());
        request.setDurationType(input.getDurationType());
        request.setStartDate(input.getStartDate());
        request.setEndDate(input.getEndDate());
        request.setStartTime(input.getDurationType() == TeacherLeaveDuration.PARTIAL_DAY ? input.getStartTime() : null);
        request.setEndTime(input.getDurationType() == TeacherLeaveDuration.PARTIAL_DAY ? input.getEndTime() : null);
        request.setReason(input.getReason().trim());
    }

    private Staff getActiveStaffMember(Long authenticatedUserId) {
        Staff staff = staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active staff profile not found for current user"));
        if (staff.getUser().getRole() != Role.TEACHER && staff.getUser().getRole() != Role.STAFF) {
            throw new IllegalStateException("Current account is not an employee self-service account");
        }
        return staff;
    }

    private TeacherLeaveRequest getRequestEntity(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher leave request not found with id: " + requestId));
    }

    private TeacherLeaveRequest getOwnedRequest(Long teacherId, Long requestId) {
        TeacherLeaveRequest request = getRequestEntity(requestId);
        if (!request.getTeacher().getId().equals(teacherId)) {
            throw new ResourceNotFoundException("Teacher leave request not found in current teacher portal");
        }
        return request;
    }

    private TeacherLeaveSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Affected teacher session not found with id: " + sessionId));
    }

    private void ensurePending(TeacherLeaveRequest request, String message) {
        if (request.getStatus() != TeacherLeaveStatus.PENDING) {
            throw new IllegalStateException(message);
        }
    }

    private TeacherLeaveRequestDTO toDto(TeacherLeaveRequest request) {
        List<TeacherLeaveSessionDTO> sessions = request.getAffectedSessions().stream()
                .map(this::toSessionDto)
                .toList();
        int coveredCount = (int) sessions.stream()
                .filter(session -> session.getCoverageStatus() != TeacherLeaveCoverageStatus.UNASSIGNED)
                .count();
        return new TeacherLeaveRequestDTO(
                request.getId(),
                request.getTeacher().getId(),
                request.getTeacher().getStaffId(),
                request.getTeacher().getName(),
                request.getTeacher().getDesignation(),
                request.getTeacher().getStaffCategory(),
                Boolean.TRUE.equals(request.getTeacher().getTeachingCapable()),
                Boolean.TRUE.equals(request.getTeacher().getTeachingCapable()),
                request.getLeaveType(),
                request.getDurationType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getReason(),
                request.getStatus(),
                request.getReviewedBy() == null ? null : request.getReviewedBy().getId(),
                request.getReviewedBy() == null ? null : request.getReviewedBy().getName(),
                request.getReviewerRemarks(),
                request.getReviewedAt(),
                sessions.size(),
                coveredCount,
                sessions,
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    private TeacherLeaveSessionDTO toSessionDto(TeacherLeaveSession session) {
        return new TeacherLeaveSessionDTO(
                session.getId(),
                session.getTimetable().getId(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getClassIdSnapshot(),
                session.getClassNameSnapshot(),
                session.getSubjectIdSnapshot(),
                session.getSubjectNameSnapshot(),
                session.getRoomNumberSnapshot(),
                session.getSubstituteTeacher() == null ? null : session.getSubstituteTeacher().getId(),
                session.getSubstituteTeacher() == null ? null : session.getSubstituteTeacher().getName(),
                session.getCoverageStatus(),
                session.getCoverageRemarks()
        );
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
