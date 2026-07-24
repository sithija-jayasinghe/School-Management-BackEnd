package org.edu.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.edu.dto.request.BulkAttendanceRequest;
import org.edu.dto.request.BulkAttendanceStudentRequest;
import org.edu.entity.Attendance;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AttendanceMapper;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.AttendanceService;
import org.edu.util.AttendanceStatus;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableRepository timetableRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    public AttendanceDTO createAttendance(Long authenticatedUserId, AttendanceDTO dto) {
        Student student = getActiveStudent(dto.getStudentId());
        validateStudentHasClass(student);
        validateClassAccess(authenticatedUserId, student.getCurrentClass().getId());
        validateDuplicate(dto, null);

        Attendance attendance = new Attendance();
        attendanceMapper.updateEntityFromDTO(dto, attendance);
        applyRelations(authenticatedUserId, attendance, dto, student);

        return attendanceMapper.toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public List<AttendanceDTO> markClassAttendance(Long authenticatedUserId, BulkAttendanceRequest request) {
        org.edu.entity.Class studentClass = classRepository.findByIdAndActiveTrue(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));
        validateClassAccess(authenticatedUserId, studentClass.getId());
        validateUniqueStudents(request.getStudents());

        Timetable timetable = resolveTimetable(request.getTimetableId(), studentClass.getId());
        Subject subject = timetable == null ? resolveSubject(request.getSubjectId()) : timetable.getSubject();
        Staff markedBy = resolveMarkedByForCurrentUser(authenticatedUserId, request.getMarkedByStaffId());

        List<Attendance> attendanceRecords = new ArrayList<>();

        for (BulkAttendanceStudentRequest studentRequest : request.getStudents()) {
            Student student = getActiveStudent(studentRequest.getStudentId());
            validateStudentBelongsToClass(student, studentClass.getId());

            AttendanceDTO duplicateCheck = new AttendanceDTO();
            duplicateCheck.setStudentId(student.getId());
            duplicateCheck.setAttendanceDate(request.getAttendanceDate());
            duplicateCheck.setTimetableId(request.getTimetableId());
            validateDuplicate(duplicateCheck, null);

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setStudentClass(studentClass);
            attendance.setSubject(subject);
            attendance.setTimetable(timetable);
            attendance.setMarkedBy(markedBy);
            attendance.setAttendanceDate(request.getAttendanceDate());
            attendance.setStatus(studentRequest.getStatus());
            attendance.setRemarks(studentRequest.getRemarks());
            attendanceRecords.add(attendance);
        }

        return attendanceRepository.saveAll(attendanceRecords)
                .stream()
                .map(attendanceMapper::toDTO)
                .toList();
    }

    @Override
    public AttendanceDTO updateAttendance(Long authenticatedUserId, Long id, AttendanceDTO dto) {
        Attendance attendance = getAttendance(id);
        validateClassAccess(authenticatedUserId, attendance.getStudentClass().getId());
        Long studentId = dto.getStudentId() == null ? attendance.getStudent().getId() : dto.getStudentId();
        Student student = getActiveStudent(studentId);
        validateStudentHasClass(student);
        validateClassAccess(authenticatedUserId, student.getCurrentClass().getId());
        validateDuplicate(dtoWithResolvedFields(dto, attendance, studentId), id);

        attendanceMapper.updateEntityFromDTO(dto, attendance);
        applyRelations(authenticatedUserId, attendance, dtoWithResolvedFields(dto, attendance, studentId), student);

        return attendanceMapper.toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public void deleteAttendance(Long authenticatedUserId, Long id) {
        Attendance attendance = getAttendance(id);
        validateClassAccess(authenticatedUserId, attendance.getStudentClass().getId());
        attendanceRepository.delete(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDTO getAttendanceById(Long authenticatedUserId, Long id) {
        Attendance attendance = getAttendance(id);
        validateClassAccess(authenticatedUserId, attendance.getStudentClass().getId());
        return attendanceMapper.toDTO(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getAllAttendance(Long authenticatedUserId, Pageable pageable) {
        List<Long> accessibleClassIds = resolveAccessibleClassIds(authenticatedUserId);
        if (accessibleClassIds != null) {
            if (accessibleClassIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return attendanceRepository.findByStudentClassIdIn(accessibleClassIds, pageable)
                    .map(attendanceMapper::toDTO);
        }
        return attendanceRepository.findAll(pageable)
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getStudentAttendance(Long authenticatedUserId, Long studentId, Pageable pageable) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        if (student.getCurrentClass() != null) {
            validateClassAccess(authenticatedUserId, student.getCurrentClass().getId());
        }

        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId, pageable)
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getClassAttendanceByDate(Long authenticatedUserId, Long classId, LocalDate attendanceDate, Pageable pageable) {
        validateClassAccess(authenticatedUserId, classId);
        return attendanceRepository.findByStudentClassIdAndAttendanceDateOrderByStudentNameAsc(
                        classId,
                        attendanceDate,
                        pageable
                )
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> filterAttendance(Long authenticatedUserId, Long classId, Long studentId, Long subjectId, Long markedByStaffId, AttendanceStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        validateOptionalDateRange(fromDate, toDate);

        if (classId != null) {
            validateClassAccess(authenticatedUserId, classId);
            return attendanceRepository
                    .filterAttendance(classId, studentId, subjectId, markedByStaffId, status, fromDate, toDate, pageable)
                    .map(attendanceMapper::toDTO);
        }

        List<Long> accessibleClassIds = resolveAccessibleClassIds(authenticatedUserId);
        if (accessibleClassIds != null) {
            if (accessibleClassIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return attendanceRepository
                    .filterAttendanceByClassIds(accessibleClassIds, studentId, subjectId, markedByStaffId, status, fromDate, toDate, pageable)
                    .map(attendanceMapper::toDTO);
        }

        return attendanceRepository
                .filterAttendance(null, studentId, subjectId, markedByStaffId, status, fromDate, toDate, pageable)
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryDTO getStudentAttendanceSummary(Long authenticatedUserId, Long studentId, LocalDate fromDate, LocalDate toDate) {
        Student student = getActiveStudent(studentId);
        if (student.getCurrentClass() != null) {
            validateClassAccess(authenticatedUserId, student.getCurrentClass().getId());
        }
        validateDateRange(fromDate, toDate);

        List<Attendance> records = attendanceRepository.findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                studentId,
                fromDate,
                toDate
        );

        long present = countByStatus(records, AttendanceStatus.PRESENT);
        long late = countByStatus(records, AttendanceStatus.LATE);
        long absent = countByStatus(records, AttendanceStatus.ABSENT);
        long excused = countByStatus(records, AttendanceStatus.EXCUSED);
        double percentage = records.isEmpty() ? 0.0 : ((double) (present + late + excused) / records.size()) * 100;

        return new AttendanceSummaryDTO(
                student.getId(),
                student.getName(),
                fromDate,
                toDate,
                records.size(),
                present,
                absent,
                late,
                excused,
                Math.round(percentage * 100.0) / 100.0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentPortalAttendanceDTO> getPortalAttendance(Long studentId, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        return attendanceRepository.findPortalAttendanceByStudentIdAndDateRange(studentId, fromDate, toDate)
                .stream()
                .map(this::toPortalAttendance)
                .toList();
    }

    private Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
    }

    /**
     * authenticatedUserId == null means a trusted internal/system caller (e.g. auto-marking
     * attendance when a leave request is approved) that already validated access elsewhere.
     */
    private void validateClassAccess(Long authenticatedUserId, Long classId) {
        if (authenticatedUserId == null) {
            return;
        }

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() != Role.TEACHER) {
            throw new ResourceNotFoundException("Attendance access is not available for current user");
        }

        Staff staff = staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"));

        boolean classTeacherAccess = classRepository.existsByIdAndClassTeacherIdAndActiveTrue(classId, staff.getId());
        boolean teachingAccess = timetableRepository.existsByStaffIdAndStudentClassId(staff.getId(), classId);

        if (!classTeacherAccess && !teachingAccess) {
            throw new ResourceNotFoundException("Class not found in current teacher attendance access");
        }
    }

    /**
     * Returns null when the caller has unrestricted access (ADMIN, or a trusted internal
     * caller with authenticatedUserId == null); otherwise returns the set of class ids the
     * teacher may see (as class teacher or via a teaching assignment/timetable entry).
     */
    private List<Long> resolveAccessibleClassIds(Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            return null;
        }

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        if (user.getRole() == Role.ADMIN) {
            return null;
        }

        if (user.getRole() != Role.TEACHER) {
            throw new ResourceNotFoundException("Attendance access is not available for current user");
        }

        Staff staff = staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"));

        Set<Long> classIds = new HashSet<>();
        classRepository.findByClassTeacherIdAndActiveTrueOrderByNameAsc(staff.getId())
                .forEach(schoolClass -> classIds.add(schoolClass.getId()));
        timetableRepository.findByStaffIdOrderByDayOfWeekAscStartTimeAsc(staff.getId())
                .forEach(timetable -> classIds.add(timetable.getStudentClass().getId()));

        return new ArrayList<>(classIds);
    }

    private Student getActiveStudent(Long studentId) {
        return studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active student not found with id: " + studentId));
    }

    private void validateStudentHasClass(Student student) {
        if (student.getCurrentClass() == null) {
            throw new IllegalStateException("Student must be assigned to an active class before marking attendance");
        }
    }

    private void validateStudentBelongsToClass(Student student, Long classId) {
        validateStudentHasClass(student);
        if (!student.getCurrentClass().getId().equals(classId)) {
            throw new IllegalArgumentException("Student " + student.getId() + " does not belong to class " + classId);
        }
    }

    private void validateUniqueStudents(List<BulkAttendanceStudentRequest> students) {
        Set<Long> studentIds = new HashSet<>();
        for (BulkAttendanceStudentRequest student : students) {
            if (!studentIds.add(student.getStudentId())) {
                throw new IllegalArgumentException("Duplicate student in attendance request: " + student.getStudentId());
            }
        }
    }

    private void validateDuplicate(AttendanceDTO dto, Long currentId) {
        if (dto.getTimetableId() == null) {
            boolean exists = currentId == null
                    ? attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIsNull(
                            dto.getStudentId(),
                            dto.getAttendanceDate()
                    )
                    : attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIsNullAndIdNot(
                            dto.getStudentId(),
                            dto.getAttendanceDate(),
                            currentId
                    );

            if (exists) {
                throw new IllegalStateException("Attendance already marked for this student and date");
            }
            return;
        }

        boolean exists = currentId == null
                ? attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableId(
                        dto.getStudentId(),
                        dto.getAttendanceDate(),
                        dto.getTimetableId()
                )
                : attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIdAndIdNot(
                        dto.getStudentId(),
                        dto.getAttendanceDate(),
                        dto.getTimetableId(),
                        currentId
                );

        if (exists) {
            throw new IllegalStateException("Attendance already marked for this student, date, and timetable period");
        }
    }

    private void applyRelations(Long authenticatedUserId, Attendance attendance, AttendanceDTO dto, Student student) {
        attendance.setStudent(student);
        attendance.setStudentClass(student.getCurrentClass());

        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + dto.getSubjectId()));
            attendance.setSubject(subject);
        }

        if (dto.getTimetableId() != null) {
            Timetable timetable = timetableRepository.findById(dto.getTimetableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with id: " + dto.getTimetableId()));
            validateTimetableMatchesStudentClass(timetable, student);
            attendance.setTimetable(timetable);
            attendance.setSubject(timetable.getSubject());
        }

        Staff markedBy = resolveMarkedByForCurrentUser(authenticatedUserId, dto.getMarkedByStaffId());
        if (markedBy != null) {
            attendance.setMarkedBy(markedBy);
        }
    }

    private void validateTimetableMatchesStudentClass(Timetable timetable, Student student) {
        if (!timetable.getStudentClass().getId().equals(student.getCurrentClass().getId())) {
            throw new IllegalArgumentException("Timetable period does not belong to the student's current class");
        }
    }

    private Timetable resolveTimetable(Long timetableId, Long classId) {
        if (timetableId == null) {
            return null;
        }

        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with id: " + timetableId));
        if (!timetable.getStudentClass().getId().equals(classId)) {
            throw new IllegalArgumentException("Timetable period does not belong to class " + classId);
        }
        return timetable;
    }

    private Subject resolveSubject(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
    }

    private Staff resolveMarkedByForCurrentUser(Long authenticatedUserId, Long fallbackStaffId) {
        if (authenticatedUserId != null) {
            User user = userRepository.findById(authenticatedUserId).orElse(null);
            if (user != null && user.getRole() == Role.TEACHER) {
                return staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId).orElse(null);
            }
        }
        return resolveMarkedBy(fallbackStaffId);
    }

    private Staff resolveMarkedBy(Long markedByStaffId) {
        if (markedByStaffId == null) {
            return null;
        }
        return staffRepository.findByIdAndActiveTrue(markedByStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + markedByStaffId));
    }

    private AttendanceDTO dtoWithResolvedFields(AttendanceDTO dto, Attendance attendance, Long studentId) {
        AttendanceDTO resolved = new AttendanceDTO();
        resolved.setStudentId(studentId);
        resolved.setAttendanceDate(dto.getAttendanceDate() == null ? attendance.getAttendanceDate() : dto.getAttendanceDate());
        resolved.setTimetableId(dto.getTimetableId() == null && attendance.getTimetable() != null
                ? attendance.getTimetable().getId()
                : dto.getTimetableId());
        resolved.setSubjectId(dto.getSubjectId());
        resolved.setMarkedByStaffId(dto.getMarkedByStaffId());
        resolved.setStatus(dto.getStatus());
        resolved.setRemarks(dto.getRemarks());
        return resolved;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("From date and to date are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be before or equal to to date");
        }
    }

    private void validateOptionalDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be before or equal to to date");
        }
    }

    private long countByStatus(List<Attendance> records, AttendanceStatus status) {
        return records.stream()
                .filter(record -> record.getStatus() == status)
                .count();
    }

    private ParentPortalAttendanceDTO toPortalAttendance(Attendance attendance) {
        return new ParentPortalAttendanceDTO(
                attendance.getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getStudentClass().getName(),
                attendance.getSubject() == null ? null : attendance.getSubject().getName(),
                attendance.getMarkedBy() == null ? null : attendance.getMarkedBy().getName(),
                attendance.getRemarks()
        );
    }
}
