package org.edu.service.impl;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.edu.entity.SystemSettings;
import org.edu.repository.SystemSettingsRepository;
import org.edu.service.SchoolDayPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SchoolDayPolicyServiceImpl implements SchoolDayPolicyService {

    private final SystemSettingsRepository systemSettingsRepository;

    @Override
    public LocalTime getSchoolStartTime() {
        LocalTime configured = getSettings().getSchoolStartTime();
        return configured == null ? DEFAULT_START_TIME : configured;
    }

    @Override
    public LocalTime getSchoolEndTime() {
        LocalTime configured = getSettings().getSchoolEndTime();
        return configured == null ? DEFAULT_END_TIME : configured;
    }

    @Override
    public void validateWithinSchoolDay(LocalTime startTime, LocalTime endTime, String activityName) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(activityName + " requires a valid start and end time");
        }

        LocalTime schoolStart = getSchoolStartTime();
        LocalTime schoolEnd = getSchoolEndTime();
        if (startTime.isBefore(schoolStart) || endTime.isAfter(schoolEnd)) {
            throw new IllegalArgumentException(
                    activityName + " must be between " + schoolStart + " and " + schoolEnd
            );
        }
    }

    private SystemSettings getSettings() {
        return systemSettingsRepository.findTopByOrderByIdAsc().orElseGet(SystemSettings::new);
    }
}
