package org.edu.service;

import java.time.LocalTime;

public interface SchoolDayPolicyService {

    LocalTime DEFAULT_START_TIME = LocalTime.of(7, 30);
    LocalTime DEFAULT_END_TIME = LocalTime.of(13, 30);

    LocalTime getSchoolStartTime();

    LocalTime getSchoolEndTime();

    void validateWithinSchoolDay(LocalTime startTime, LocalTime endTime, String activityName);
}
