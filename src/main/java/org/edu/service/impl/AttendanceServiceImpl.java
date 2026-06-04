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
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AttendanceMapper;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.AttendanceService;
import org.edu.util.AttendanceStatus;
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
    private final AttendanceMapper attendanceMapper;

    @Override
    public AttendanceDTO createAttendance(AttendanceDTO dto) {
        Student student = getActiveStudent(dto.getStudentId());
        validateStudentHasClass(student);
        validateDuplicate(dto, null);

        Attendance attendance = new Attendance();
        attendanceMapper.updateEntityFromDTO(dto, attendance);
        applyRelations(attendance, dto, student);

        return attendanceMapper.toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public List<AttendanceDTO> markClassAttendance(BulkAttendanceRequest request) {
        org.edu.entity.Class studentClass = classRepository.findByIdAndActiveTrue(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));
        validateUniqueStudents(request.getStudents());

        Timetable timetable = resolveTimetable(request.getTimetableId(), studentClass.getId());
        Subject subject = timetable == null ? resolveSubject(request.getSubjectId()) : timetable.getSubject();
        Staff markedBy = resolveMarkedBy(request.getMarkedByStaffId());

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
    public AttendanceDTO updateAttendance(Long id, AttendanceDTO dto) {
        Attendance attendance = getAttendance(id);
        Long studentId = dto.getStudentId() == null ? attendance.getStudent().getId() : dto.getStudentId();
        Student student = getActiveStudent(studentId);
        validateStudentHasClass(student);
        validateDuplicate(dtoWithResolvedFields(dto, attendance, studentId), id);

        attendanceMapper.updateEntityFromDTO(dto, attendance);
        applyRelations(attendance, dtoWithResolvedFields(dto, attendance, studentId), student);

        return attendanceMapper.toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public void deleteAttendance(Long id) {
        Attendance attendance = getAttendance(id);
        attendanceRepository.delete(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDTO getAttendanceById(Long id) {
        return attendanceMapper.toDTO(getAttendance(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getAllAttendance(Pageable pageable) {
        return attendanceRepository.findAll(pageable)
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getStudentAttendance(Long studentId, Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId, pageable)
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> getClassAttendanceByDate(Long classId, LocalDate attendanceDate, Pageable pageable) {
        return attendanceRepository.findByStudentClassIdAndAttendanceDateOrderByStudentNameAsc(
                        classId,
                        attendanceDate,
                        pageable
                )
                .map(attendanceMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryDTO getStudentAttendanceSummary(Long studentId, LocalDate fromDate, LocalDate toDate) {
        Student student = getActiveStudent(studentId);
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

    private void applyRelations(Attendance attendance, AttendanceDTO dto, Student student) {
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

        if (dto.getMarkedByStaffId() != null) {
            Staff markedBy = staffRepository.findByIdAndActiveTrue(dto.getMarkedByStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + dto.getMarkedByStaffId()));
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
