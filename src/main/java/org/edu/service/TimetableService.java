package org.edu.service;

import org.edu.dto.TimetableDTO;

import java.util.List;

public interface TimetableService {

    TimetableDTO createTimetable(TimetableDTO dto);

    TimetableDTO updateTimetable(Long id, TimetableDTO dto);

    void deleteTimetable(Long id);

    TimetableDTO getTimetableById(Long id);

    List<TimetableDTO> getClassSchedule(Long classId);

    List<TimetableDTO> getTeacherSchedule(Long staffId);

    // Add additional advanced methods if needed, like getLiveStatus
}
