package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;
import org.edu.dto.SystemSettingsDTO;
import org.edu.dto.request.SystemSettingsUpdateRequest;
import org.edu.entity.AcademicYear;
import org.edu.entity.SystemSettings;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.SystemSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemSettingsServiceImplTest {

    @Mock
    private SystemSettingsRepository systemSettingsRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @InjectMocks
    private SystemSettingsServiceImpl systemSettingsService;

    @BeforeEach
    void setUp() {
        lenient().when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.empty());
        lenient().when(systemSettingsRepository.save(any(SystemSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreateDefaultSettingsWhenMissing() {
        SystemSettings saved = new SystemSettings();
        saved.setId(1L);
        saved.setDefaultLanguage("en");
        saved.setTimeZone("Asia/Colombo");
        saved.setSchoolStartTime(LocalTime.of(7, 30));
        saved.setSchoolEndTime(LocalTime.of(13, 30));

        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(systemSettingsRepository.save(any(SystemSettings.class))).thenReturn(saved);

        SystemSettingsDTO response = systemSettingsService.getSystemSettings();

        assertEquals(1L, response.getId());
        assertEquals("en", response.getDefaultLanguage());
        assertEquals("Asia/Colombo", response.getTimeZone());
        assertEquals(LocalTime.of(7, 30), response.getSchoolStartTime());
        assertEquals(LocalTime.of(13, 30), response.getSchoolEndTime());
        verify(systemSettingsRepository).save(any(SystemSettings.class));
    }

    @Test
    void shouldUpdateExistingSystemSettings() {
        SystemSettings settings = new SystemSettings();
        settings.setId(1L);
        settings.setDefaultLanguage("en");
        settings.setTimeZone("Asia/Colombo");

        SystemSettingsUpdateRequest request = new SystemSettingsUpdateRequest();
        request.setSchoolName("  Western Province Model School  ");
        request.setSchoolCode("  WPMS-001 ");
        request.setEmail("  ADMIN@SMS.LK ");
        request.setSchoolStartTime(LocalTime.of(7, 30));
        request.setSchoolEndTime(LocalTime.of(13, 30));
        request.setAttendanceCutoffTime(LocalTime.of(8, 0));

        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings));
        when(systemSettingsRepository.save(settings)).thenReturn(settings);

        SystemSettingsDTO response = systemSettingsService.updateSystemSettings(request);

        assertEquals("Western Province Model School", response.getSchoolName());
        assertEquals("WPMS-001", response.getSchoolCode());
        assertEquals("admin@sms.lk", response.getEmail());
        assertEquals(LocalTime.of(8, 0), response.getAttendanceCutoffTime());
    }

    @Test
    void shouldDeriveCurrentAcademicYearLabelFromAcademicCalendar() {
        SystemSettings settings = new SystemSettings();
        settings.setId(1L);
        settings.setCurrentAcademicYearLabel("Legacy 2025");
        settings.setDefaultLanguage("en");
        settings.setTimeZone("Asia/Colombo");

        AcademicYear currentAcademicYear = new AcademicYear();
        currentAcademicYear.setId(2L);
        currentAcademicYear.setName("2026");
        currentAcademicYear.setCurrent(true);
        currentAcademicYear.setActive(true);

        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(currentAcademicYear));

        SystemSettingsDTO response = systemSettingsService.getSystemSettings();

        assertEquals("2026", response.getCurrentAcademicYearLabel());
    }

    @Test
    void shouldAllowClearingOptionalTextFieldsWithBlankInput() {
        SystemSettings settings = new SystemSettings();
        settings.setId(1L);
        settings.setSchoolName("Old Name");
        settings.setDefaultLanguage("en");
        settings.setTimeZone("Asia/Colombo");

        SystemSettingsUpdateRequest request = new SystemSettingsUpdateRequest();
        request.setSchoolName("   ");

        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings));
        when(systemSettingsRepository.save(settings)).thenReturn(settings);

        SystemSettingsDTO response = systemSettingsService.updateSystemSettings(request);

        assertNull(response.getSchoolName());
    }

    @Test
    void shouldRejectInvalidTimeWindow() {
        SystemSettings settings = new SystemSettings();
        settings.setId(1L);
        settings.setDefaultLanguage("en");
        settings.setTimeZone("Asia/Colombo");

        SystemSettingsUpdateRequest request = new SystemSettingsUpdateRequest();
        request.setSchoolStartTime(LocalTime.of(14, 0));
        request.setSchoolEndTime(LocalTime.of(13, 0));

        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings));

        assertThrows(IllegalArgumentException.class, () -> systemSettingsService.updateSystemSettings(request));
    }

    @Test
    void shouldAllowUpdatingSchoolHoursEvenWhenExistingTimetableNeedsCleanup() {
        SystemSettings settings = new SystemSettings();
        settings.setId(1L);
        settings.setDefaultLanguage("en");
        settings.setTimeZone("Asia/Colombo");
        settings.setSchoolStartTime(LocalTime.of(7, 30));
        settings.setSchoolEndTime(LocalTime.of(13, 30));

        SystemSettingsUpdateRequest request = new SystemSettingsUpdateRequest();
        request.setSchoolStartTime(LocalTime.of(8, 0));
        request.setSchoolEndTime(LocalTime.of(13, 0));

        when(systemSettingsRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(settings));
        when(systemSettingsRepository.save(settings)).thenReturn(settings);

        SystemSettingsDTO response = systemSettingsService.updateSystemSettings(request);

        assertEquals(LocalTime.of(8, 0), response.getSchoolStartTime());
        assertEquals(LocalTime.of(13, 0), response.getSchoolEndTime());
    }
}
