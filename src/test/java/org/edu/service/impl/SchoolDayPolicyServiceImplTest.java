package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;
import org.edu.entity.SystemSettings;
import org.edu.repository.SystemSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchoolDayPolicyServiceImplTest {

    @Mock
    private SystemSettingsRepository systemSettingsRepository;

    @Test
    void shouldUseGovernmentSchoolOperatingHoursWhenSettingsAreMissing() {
        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        SchoolDayPolicyServiceImpl service = new SchoolDayPolicyServiceImpl(systemSettingsRepository);

        assertEquals(LocalTime.of(7, 30), service.getSchoolStartTime());
        assertEquals(LocalTime.of(13, 30), service.getSchoolEndTime());
    }

    @Test
    void shouldAcceptActivityInsideConfiguredSchoolDay() {
        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings()));
        SchoolDayPolicyServiceImpl service = new SchoolDayPolicyServiceImpl(systemSettingsRepository);

        assertDoesNotThrow(() -> service.validateWithinSchoolDay(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                "Timetable period"
        ));
    }

    @Test
    void shouldRejectActivityOutsideConfiguredSchoolDay() {
        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings()));
        SchoolDayPolicyServiceImpl service = new SchoolDayPolicyServiceImpl(systemSettingsRepository);

        assertThrows(IllegalArgumentException.class, () -> service.validateWithinSchoolDay(
                LocalTime.of(7, 0),
                LocalTime.of(8, 0),
                "Timetable period"
        ));
    }

    private SystemSettings settings() {
        SystemSettings settings = new SystemSettings();
        settings.setSchoolStartTime(LocalTime.of(7, 30));
        settings.setSchoolEndTime(LocalTime.of(13, 30));
        return settings;
    }
}
