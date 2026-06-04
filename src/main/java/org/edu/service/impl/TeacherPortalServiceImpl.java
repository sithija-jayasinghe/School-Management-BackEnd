package org.edu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.teacherportal.TeacherPortalClassSummaryDTO;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.entity.Staff;
import org.edu.entity.Timetable;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.TeacherPortalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeacherPortalServiceImpl implements TeacherPortalService {

    private final StaffRepository staffRepository;
    private final ClassRepository classRepository;
    private final TimetableRepository timetableRepository;

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
}
