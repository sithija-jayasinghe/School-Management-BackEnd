package org.edu.service.impl;

import org.edu.dto.TimetableDTO;
import org.edu.entity.Class;
import org.edu.entity.Staff;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.mapper.TimetableMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TeacherLeaveSessionRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.TimetableService;
import org.edu.service.SchoolDayPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableServiceImpl implements TimetableService {

    @Autowired
    private TimetableRepository timetableRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private TeacherLeaveSessionRepository teacherLeaveSessionRepository;
    @Autowired
    private TimetableMapper timetableMapper;
    @Autowired
    private SchoolDayPolicyService schoolDayPolicyService;

    @Override
    public TimetableDTO createTimetable(TimetableDTO dto) {
        validateConflicts(dto, null);

        Timetable timetable = mapRelations(dto, new Timetable());
        return timetableMapper.toDTO(timetableRepository.save(timetable));
    }

    @Override
    public TimetableDTO updateTimetable(Long id, TimetableDTO dto) {
        Timetable existing = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found"));

        validateConflicts(dto, id);

        Timetable timetable = mapRelations(dto, existing);
        return timetableMapper.toDTO(timetableRepository.save(timetable));
    }

    @Override
    public void deleteTimetable(Long id) {
        if (!timetableRepository.existsById(id)) {
            throw new RuntimeException("Timetable entry not found");
        }
        attendanceRepository.detachTimetable(id);
        teacherLeaveSessionRepository.deleteByTimetableId(id);
        timetableRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableDTO getTimetableById(Long id) {
        return timetableRepository.findById(id)
                .map(timetableMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableDTO> getClassSchedule(Long classId) {
        return timetableRepository.findByStudentClassIdOrderByDayOfWeekAscStartTimeAsc(classId)
                .stream().map(timetableMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableDTO> getTeacherSchedule(Long staffId) {
        return timetableRepository.findByStaffIdOrderByDayOfWeekAscStartTimeAsc(staffId)
                .stream().map(timetableMapper::toDTO).collect(Collectors.toList());
    }

    private void validateConflicts(TimetableDTO dto, Long currentId) {
        schoolDayPolicyService.validateWithinSchoolDay(dto.getStartTime(), dto.getEndTime(), "Timetable period");

        // Check Teacher Conflict
        List<Timetable> teacherConflicts = timetableRepository.findTeacherConflicts(
                dto.getStaffId(), dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());

        if (teacherConflicts.stream().anyMatch(t -> currentId == null || !t.getId().equals(currentId))) {
            throw new RuntimeException("Staff member is already teaching another class at this time.");
        }

        // Check Class Conflict
        List<Timetable> classConflicts = timetableRepository.findClassConflicts(
                dto.getClassId(), dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());

        if (classConflicts.stream().anyMatch(t -> currentId == null || !t.getId().equals(currentId))) {
            throw new RuntimeException("This class already has a subject scheduled at this time.");
        }
    }

    private Timetable mapRelations(TimetableDTO dto, Timetable timetable) {
        Class aClass = classRepository.findByIdAndActiveTrue(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Active class not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        Staff staff = staffRepository.findByIdAndActiveTrue(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("Active staff not found"));

        timetable.setStudentClass(aClass);
        timetable.setSubject(subject);
        timetable.setStaff(staff);
        timetable.setDayOfWeek(dto.getDayOfWeek());
        timetable.setStartTime(dto.getStartTime());
        timetable.setEndTime(dto.getEndTime());
        timetable.setRoomNumber(dto.getRoomNumber());

        return timetable;
    }
}
