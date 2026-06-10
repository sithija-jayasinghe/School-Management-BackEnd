package org.edu.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.AcademicReportSubjectDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.AcademicReportGenerateRequest;
import org.edu.dto.request.AcademicReportUpdateRequest;
import org.edu.entity.AcademicReport;
import org.edu.entity.AcademicReportSubject;
import org.edu.entity.AcademicTerm;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.StudentMark;
import org.edu.entity.Subject;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.AcademicReportRepository;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentMarkRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.AcademicReportPdfService;
import org.edu.service.AcademicReportService;
import org.edu.service.AttendanceService;
import org.edu.util.AcademicReportStatus;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicReportServiceImpl implements AcademicReportService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final AcademicReportRepository academicReportRepository;
    private final AcademicTermRepository academicTermRepository;
    private final StudentRepository studentRepository;
    private final StudentMarkRepository studentMarkRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ClassRepository classRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceService attendanceService;
    private final AcademicReportPdfService academicReportPdfService;

    @Override
    public AcademicReportDTO generateReport(Long authenticatedUserId, AcademicReportGenerateRequest request) {
        User user = getUser(authenticatedUserId);
        Student student = getActiveStudent(request.getStudentId());
        AcademicTerm term = getActiveTerm(request.getAcademicTermId());
        validateStudentClass(student);
        validateReportAccess(user, student.getCurrentClass().getId());

        if (academicReportRepository.existsByStudentIdAndAcademicTermId(student.getId(), term.getId())) {
            throw new IllegalStateException("Academic report already exists for this student and term");
        }

        AcademicReport report = new AcademicReport();
        report.setStudent(student);
        report.setAcademicTerm(term);
        report.setStudentClass(student.getCurrentClass());
        report.setGeneratedBy(user);
        report.setClassTeacherRemarks(request.getClassTeacherRemarks());
        report.setPrincipalRemarks(request.getPrincipalRemarks());
        report.setStatus(AcademicReportStatus.DRAFT);
        populateSnapshot(report);

        return toDTO(academicReportRepository.save(report));
    }

    @Override
    public AcademicReportDTO regenerateReport(Long authenticatedUserId, Long reportId) {
        User user = getUser(authenticatedUserId);
        AcademicReport report = getReportEntity(reportId);
        validateReportAccess(user, report.getStudentClass().getId());
        validateDraft(report);
        populateSnapshot(report);
        return toDTO(academicReportRepository.save(report));
    }

    @Override
    public AcademicReportDTO updateReport(
            Long authenticatedUserId,
            Long reportId,
            AcademicReportUpdateRequest request
    ) {
        User user = getUser(authenticatedUserId);
        AcademicReport report = getReportEntity(reportId);
        validateReportAccess(user, report.getStudentClass().getId());
        validateDraft(report);

        if (request.getClassTeacherRemarks() != null) {
            report.setClassTeacherRemarks(request.getClassTeacherRemarks());
        }
        if (request.getPrincipalRemarks() != null) {
            report.setPrincipalRemarks(request.getPrincipalRemarks());
        }
        return toDTO(academicReportRepository.save(report));
    }

    @Override
    public AcademicReportDTO publishReport(Long authenticatedUserId, Long reportId) {
        User user = getUser(authenticatedUserId);
        AcademicReport report = getReportEntity(reportId);
        validatePublishAccess(user, report.getStudentClass().getId());
        validateDraft(report);
        if (report.getSubjects().isEmpty()) {
            throw new IllegalStateException("Cannot publish an academic report without subject results");
        }

        report.setStatus(AcademicReportStatus.PUBLISHED);
        report.setPublishedBy(user);
        report.setPublishedAt(LocalDateTime.now());
        return toDTO(academicReportRepository.save(report));
    }

    @Override
    public void deleteDraftReport(Long authenticatedUserId, Long reportId) {
        User user = getUser(authenticatedUserId);
        AcademicReport report = getReportEntity(reportId);
        validateReportAccess(user, report.getStudentClass().getId());
        validateDraft(report);
        academicReportRepository.delete(report);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicReportDTO getReport(Long authenticatedUserId, Long reportId) {
        User user = getUser(authenticatedUserId);
        AcademicReport report = getReportEntity(reportId);
        validateReportAccess(user, report.getStudentClass().getId());
        return toDTO(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicReportDTO> getStudentReports(
            Long authenticatedUserId,
            Long studentId,
            Pageable pageable
    ) {
        User user = getUser(authenticatedUserId);
        Student student = getActiveStudent(studentId);
        validateStudentClass(student);
        validateReportAccess(user, student.getCurrentClass().getId());
        return academicReportRepository.findByStudentIdOrderByAcademicTermStartDateDesc(studentId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicReportDTO> getClassReports(
            Long authenticatedUserId,
            Long classId,
            Long academicTermId,
            Pageable pageable
    ) {
        User user = getUser(authenticatedUserId);
        if (classRepository.findByIdAndActiveTrue(classId).isEmpty()) {
            throw new ResourceNotFoundException("Active class not found with id: " + classId);
        }
        getActiveTerm(academicTermId);
        validateReportAccess(user, classId);
        return academicReportRepository
                .findByStudentClassIdAndAcademicTermIdOrderByStudentNameAsc(classId, academicTermId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicReportDTO> getPublishedStudentReports(Long studentId, Pageable pageable) {
        getActiveStudent(studentId);
        return academicReportRepository.findByStudentIdAndStatusOrderByAcademicTermStartDateDesc(
                        studentId,
                        AcademicReportStatus.PUBLISHED,
                        pageable
                )
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicReportDTO getPublishedReport(Long reportId) {
        return toDTO(getPublishedReportEntity(reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentFileResponse downloadReportCard(Long authenticatedUserId, Long reportId) {
        User user = getUser(authenticatedUserId);
        AcademicReport report = getReportEntity(reportId);
        validateReportAccess(user, report.getStudentClass().getId());
        return academicReportPdfService.generate(report);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentFileResponse downloadPublishedReportCard(Long reportId) {
        return academicReportPdfService.generate(getPublishedReportEntity(reportId));
    }

    private void populateSnapshot(AcademicReport report) {
        List<StudentMark> marks = studentMarkRepository.findReportMarksByStudentIdAndAcademicTermId(
                report.getStudent().getId(),
                report.getAcademicTerm().getId()
        );
        if (marks.isEmpty()) {
            throw new IllegalStateException("No marks are available for this student and academic term");
        }

        Map<Long, List<StudentMark>> marksBySubject = new LinkedHashMap<>();
        marks.forEach(mark -> marksBySubject
                .computeIfAbsent(mark.getExam().getSubject().getId(), ignored -> new ArrayList<>())
                .add(mark));

        List<AcademicReportSubject> subjectRows = marksBySubject.values()
                .stream()
                .map(this::buildSubjectSnapshot)
                .toList();
        report.replaceSubjects(subjectRows);

        BigDecimal totalObtained = subjectRows.stream()
                .map(AcademicReportSubject::getTotalMarksObtained)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMaximum = subjectRows.stream()
                .map(AcademicReportSubject::getTotalMaxMarks)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallPercentage = percentage(totalObtained, totalMaximum);
        int passedSubjects = (int) subjectRows.stream().filter(AcademicReportSubject::isPassed).count();

        report.setOverallPercentage(overallPercentage);
        report.setOverallGrade(calculateGrade(overallPercentage));
        report.setSubjectCount(subjectRows.size());
        report.setPassedSubjectCount(passedSubjects);
        report.setFailedSubjectCount(subjectRows.size() - passedSubjects);

        AttendanceSummaryDTO attendance = attendanceService.getStudentAttendanceSummary(
                report.getStudent().getId(),
                report.getAcademicTerm().getStartDate(),
                report.getAcademicTerm().getEndDate()
        );
        report.setTotalAttendanceRecords(attendance.getTotalRecords());
        report.setPresentCount(attendance.getPresentCount());
        report.setAbsentCount(attendance.getAbsentCount());
        report.setLateCount(attendance.getLateCount());
        report.setExcusedCount(attendance.getExcusedCount());
        report.setAttendancePercentage(BigDecimal.valueOf(attendance.getAttendancePercentage()).setScale(2, RoundingMode.HALF_UP));
    }

    private AcademicReportSubject buildSubjectSnapshot(List<StudentMark> subjectMarks) {
        Subject subject = subjectMarks.get(0).getExam().getSubject();
        BigDecimal totalObtained = subjectMarks.stream()
                .map(StudentMark::getMarksObtained)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMaximum = subjectMarks.stream()
                .map(mark -> mark.getExam().getMaxMarks())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subjectPercentage = percentage(totalObtained, totalMaximum);

        AcademicReportSubject row = new AcademicReportSubject();
        row.setSubjectId(subject.getId());
        row.setSubjectCode(subject.getCode());
        row.setSubjectName(subject.getName());
        row.setExamCount(subjectMarks.size());
        row.setTotalMarksObtained(totalObtained.setScale(2, RoundingMode.HALF_UP));
        row.setTotalMaxMarks(totalMaximum.setScale(2, RoundingMode.HALF_UP));
        row.setPercentage(subjectPercentage);
        row.setGrade(calculateGrade(subjectPercentage));
        row.setPassed(subjectMarks.stream().allMatch(StudentMark::isPassed));
        return row;
    }

    private BigDecimal percentage(BigDecimal obtained, BigDecimal maximum) {
        if (maximum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return obtained.multiply(ONE_HUNDRED).divide(maximum, 2, RoundingMode.HALF_UP);
    }

    private String calculateGrade(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "A";
        }
        if (percentage.compareTo(BigDecimal.valueOf(65)) >= 0) {
            return "B";
        }
        if (percentage.compareTo(BigDecimal.valueOf(55)) >= 0) {
            return "C";
        }
        if (percentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "S";
        }
        return "F";
    }

    private User getUser(Long authenticatedUserId) {
        return userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private Student getActiveStudent(Long studentId) {
        return studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active student not found with id: " + studentId));
    }

    private AcademicTerm getActiveTerm(Long academicTermId) {
        return academicTermRepository.findByIdAndActiveTrue(academicTermId)
                .orElseThrow(() -> new ResourceNotFoundException("Active academic term not found with id: " + academicTermId));
    }

    private AcademicReport getReportEntity(Long reportId) {
        return academicReportRepository.findDetailById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic report not found with id: " + reportId));
    }

    private AcademicReport getPublishedReportEntity(Long reportId) {
        AcademicReport report = getReportEntity(reportId);
        if (report.getStatus() != AcademicReportStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published academic report not found with id: " + reportId);
        }
        return report;
    }

    private void validateStudentClass(Student student) {
        if (student.getCurrentClass() == null) {
            throw new IllegalStateException("Student must be assigned to a class before generating a report");
        }
    }

    private void validateDraft(AcademicReport report) {
        if (report.getStatus() != AcademicReportStatus.DRAFT) {
            throw new IllegalStateException("Published academic reports cannot be modified");
        }
    }

    private void validateReportAccess(User user, Long classId) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        Staff staff = getTeacher(user);
        boolean classTeacher = classRepository.existsByIdAndClassTeacherIdAndActiveTrue(classId, staff.getId());
        boolean teachesClass = timetableRepository.existsByStaffIdAndStudentClassId(staff.getId(), classId);
        if (!classTeacher && !teachesClass) {
            throw new ResourceNotFoundException("Academic report not found in current teacher access");
        }
    }

    private void validatePublishAccess(User user, Long classId) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        Staff staff = getTeacher(user);
        if (!classRepository.existsByIdAndClassTeacherIdAndActiveTrue(classId, staff.getId())) {
            throw new ResourceNotFoundException("Only the class teacher can publish this academic report");
        }
    }

    private Staff getTeacher(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new ResourceNotFoundException("Academic report access is not available for current user");
        }
        return staffRepository.findByUser_IdAndActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"));
    }

    private AcademicReportDTO toDTO(AcademicReport report) {
        List<AcademicReportSubjectDTO> subjects = report.getSubjects()
                .stream()
                .map(subject -> new AcademicReportSubjectDTO(
                        subject.getSubjectId(),
                        subject.getSubjectCode(),
                        subject.getSubjectName(),
                        subject.getExamCount(),
                        subject.getTotalMarksObtained(),
                        subject.getTotalMaxMarks(),
                        subject.getPercentage(),
                        subject.getGrade(),
                        subject.isPassed()
                ))
                .toList();

        return new AcademicReportDTO(
                report.getId(),
                report.getStudent().getId(),
                report.getStudent().getName(),
                report.getStudentClass().getId(),
                report.getStudentClass().getName(),
                report.getAcademicTerm().getAcademicYear().getId(),
                report.getAcademicTerm().getAcademicYear().getName(),
                report.getAcademicTerm().getId(),
                report.getAcademicTerm().getName(),
                report.getAcademicTerm().getStartDate(),
                report.getAcademicTerm().getEndDate(),
                report.getStatus(),
                report.getOverallPercentage(),
                report.getOverallGrade(),
                report.getSubjectCount(),
                report.getPassedSubjectCount(),
                report.getFailedSubjectCount(),
                report.getTotalAttendanceRecords(),
                report.getPresentCount(),
                report.getAbsentCount(),
                report.getLateCount(),
                report.getExcusedCount(),
                report.getAttendancePercentage(),
                report.getClassTeacherRemarks(),
                report.getPrincipalRemarks(),
                report.getGeneratedBy().getId(),
                report.getGeneratedBy().getName(),
                report.getPublishedBy() == null ? null : report.getPublishedBy().getId(),
                report.getPublishedBy() == null ? null : report.getPublishedBy().getName(),
                report.getPublishedAt(),
                subjects,
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
