package org.edu.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.request.BulkAttendanceRequest;
import org.edu.dto.teacherportal.TeacherPortalClassSummaryDTO;
import org.edu.dto.teacherportal.TeacherPortalBulkAttendanceRequest;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalExamDTO;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalStudentDTO;
import org.edu.dto.teacherportal.TeacherPortalSubjectDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.entity.Exam;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.ExamRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.AttendanceService;
import org.edu.service.TeacherPortalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeacherPortalServiceImpl implements TeacherPortalService {

    private final StaffRepository staffRepository;
    private final ClassRepository classRepository;
    private final TimetableRepository timetableRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final AttendanceService attendanceService;

    @Override
    public TeacherPortalProfileDTO getProfile(Long authenticatedUserId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        return toProfile(staff);
    }

    @Override
    public TeacherPortalDashboardDTO getDashboard(Long authenticatedUserId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        List<TeacherPortalClassSummaryDTO> assignedClasses = getAssignedClassesByStaffId(staff.getId());
        List<TeacherPortalTimetableEntryDTO> schedule = getScheduleByStaffId(staff.getId());

        return new TeacherPortalDashboardDTO(
                toProfile(staff),
                assignedClasses.size(),
                schedule.size(),
                assignedClasses,
                schedule
        );
    }

    @Override
    public List<TeacherPortalClassSummaryDTO> getAssignedClasses(Long authenticatedUserId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        return getAssignedClassesByStaffId(staff.getId());
    }

    @Override
    public List<TeacherPortalTimetableEntryDTO> getSchedule(Long authenticatedUserId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        return getScheduleByStaffId(staff.getId());
    }

    @Override
    public List<TeacherPortalSubjectDTO> getSubjects(Long authenticatedUserId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        List<Timetable> schedule = timetableRepository.findTeacherPortalScheduleByStaffId(staff.getId());

        Map<Long, List<Timetable>> timetablesBySubject = schedule.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        timetable -> timetable.getSubject().getId()
                ));

        return timetablesBySubject.values()
                .stream()
                .map(this::toSubjectSummary)
                .sorted(Comparator.comparing(TeacherPortalSubjectDTO::getSubjectName))
                .toList();
    }

    @Override
    public List<TeacherPortalStudentDTO> getClassStudents(Long authenticatedUserId, Long classId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        validateTeacherClassAccess(staff.getId(), classId);

        return studentRepository.findByCurrentClassIdAndActiveTrueOrderByNameAsc(classId)
                .stream()
                .map(this::toStudentSummary)
                .toList();
    }

    @Override
    public List<TeacherPortalExamDTO> getExams(Long authenticatedUserId) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        return examRepository.findTeacherPortalExamsByStaffId(staff.getId())
                .stream()
                .map(this::toExamSummary)
                .toList();
    }

    @Override
    public Page<AttendanceDTO> getClassAttendanceByDate(
            Long authenticatedUserId,
            Long classId,
            LocalDate attendanceDate,
            Pageable pageable
    ) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        validateTeacherClassAccess(staff.getId(), classId);
        return attendanceService.getClassAttendanceByDate(classId, attendanceDate, pageable);
    }

    @Override
    public Page<AttendanceDTO> getStudentAttendance(Long authenticatedUserId, Long studentId, Pageable pageable) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        Student student = getAccessibleStudent(staff.getId(), studentId);
        return attendanceService.getStudentAttendance(student.getId(), pageable);
    }

    @Override
    public AttendanceSummaryDTO getStudentAttendanceSummary(
            Long authenticatedUserId,
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        Student student = getAccessibleStudent(staff.getId(), studentId);
        return attendanceService.getStudentAttendanceSummary(student.getId(), fromDate, toDate);
    }

    @Override
    @Transactional
    public List<AttendanceDTO> markClassAttendance(
            Long authenticatedUserId,
            Long classId,
            TeacherPortalBulkAttendanceRequest request
    ) {
        Staff staff = getActiveTeacherByUserId(authenticatedUserId);
        validateTeacherAttendanceMarkingAccess(staff.getId(), classId, request.getTimetableId());

        BulkAttendanceRequest bulkRequest = new BulkAttendanceRequest();
        bulkRequest.setClassId(classId);
        bulkRequest.setSubjectId(request.getSubjectId());
        bulkRequest.setTimetableId(request.getTimetableId());
        bulkRequest.setMarkedByStaffId(staff.getId());
        bulkRequest.setAttendanceDate(request.getAttendanceDate());
        bulkRequest.setStudents(request.getStudents());

        return attendanceService.markClassAttendance(bulkRequest);
    }

    private Staff getActiveTeacherByUserId(Long authenticatedUserId) {
        return staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"));
    }

    private List<TeacherPortalClassSummaryDTO> getAssignedClassesByStaffId(Long staffId) {
        return classRepository.findByClassTeacherIdAndActiveTrueOrderByNameAsc(staffId)
                .stream()
                .map(studentClass -> new TeacherPortalClassSummaryDTO(
                        studentClass.getId(),
                        studentClass.getName(),
                        studentClass.getStudents() == null ? 0 : studentClass.getStudents().size(),
                        studentClass.getSubjects() == null ? 0 : studentClass.getSubjects().size()
                ))
                .toList();
    }

    private List<TeacherPortalTimetableEntryDTO> getScheduleByStaffId(Long staffId) {
        return timetableRepository.findTeacherPortalScheduleByStaffId(staffId)
                .stream()
                .map(this::toScheduleEntry)
                .toList();
    }

    private TeacherPortalProfileDTO toProfile(Staff staff) {
        return new TeacherPortalProfileDTO(
                staff.getId(),
                staff.getUser().getId(),
                staff.getStaffId(),
                staff.getName(),
                staff.getUser().getEmail(),
                staff.getPhoneNumber(),
                staff.getDesignation(),
                staff.isActive(),
                staff.getCreatedAt(),
                staff.getUpdatedAt()
        );
    }

    private TeacherPortalSubjectDTO toSubjectSummary(List<Timetable> subjectTimetables) {
        Timetable firstTimetable = subjectTimetables.get(0);
        Subject subject = firstTimetable.getSubject();
        long classCount = subjectTimetables.stream()
                .map(timetable -> timetable.getStudentClass().getId())
                .distinct()
                .count();

        return new TeacherPortalSubjectDTO(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getDescription(),
                (int) classCount,
                subjectTimetables.size()
        );
    }

    private TeacherPortalStudentDTO toStudentSummary(Student student) {
        return new TeacherPortalStudentDTO(
                student.getId(),
                student.getName(),
                student.getDateOfBirth(),
                student.getPhoneNumber(),
                student.isActive(),
                student.getCurrentClass() == null ? null : student.getCurrentClass().getId(),
                student.getCurrentClass() == null ? null : student.getCurrentClass().getName()
        );
    }

    private TeacherPortalTimetableEntryDTO toScheduleEntry(Timetable timetable) {
        return new TeacherPortalTimetableEntryDTO(
                timetable.getId(),
                timetable.getStudentClass().getId(),
                timetable.getStudentClass().getName(),
                timetable.getSubject().getId(),
                timetable.getSubject().getName(),
                timetable.getDayOfWeek(),
                timetable.getStartTime(),
                timetable.getEndTime(),
                timetable.getRoomNumber()
        );
    }

    private TeacherPortalExamDTO toExamSummary(Exam exam) {
        return new TeacherPortalExamDTO(
                exam.getId(),
                exam.getName(),
                exam.getType(),
                exam.getExamDate(),
                exam.getAcademicYear().getName(),
                exam.getAcademicTerm().getName(),
                exam.getStudentClass().getId(),
                exam.getStudentClass().getName(),
                exam.getSubject().getId(),
                exam.getSubject().getCode(),
                exam.getSubject().getName(),
                exam.getMaxMarks(),
                exam.getPassMarks(),
                exam.isActive()
        );
    }

    private void validateTeacherClassAccess(Long staffId, Long classId) {
        boolean classTeacherAccess = isClassTeacherAccess(staffId, classId);
        boolean teachingAccess = isTeachingAccess(staffId, classId);

        if (!classTeacherAccess && !teachingAccess) {
            throw new ResourceNotFoundException("Class not found in current teacher portal");
        }
    }

    private Student getAccessibleStudent(Long staffId, Long studentId) {
        Student student = studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active student not found with id: " + studentId));

        if (student.getCurrentClass() == null) {
            throw new ResourceNotFoundException("Student not found in current teacher portal");
        }

        validateTeacherClassAccess(staffId, student.getCurrentClass().getId());
        return student;
    }

    private void validateTeacherAttendanceMarkingAccess(Long staffId, Long classId, Long timetableId) {
        if (timetableId == null) {
            if (!isClassTeacherAccess(staffId, classId)) {
                throw new ResourceNotFoundException("Daily attendance marking is only available to the class teacher");
            }
            return;
        }

        boolean timetableAccess = timetableRepository.existsByIdAndStaffIdAndStudentClassId(timetableId, staffId, classId);
        if (!timetableAccess) {
            throw new ResourceNotFoundException("Timetable entry not found in current teacher portal");
        }
    }

    private boolean isClassTeacherAccess(Long staffId, Long classId) {
        return classRepository.existsByIdAndClassTeacherIdAndActiveTrue(classId, staffId);
    }

    private boolean isTeachingAccess(Long staffId, Long classId) {
        return timetableRepository.existsByStaffIdAndStudentClassId(staffId, classId);
    }
}
