package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.SystemSettingsDTO;
import org.edu.dto.request.SystemSettingsUpdateRequest;
import org.edu.entity.SystemSettings;
import org.edu.repository.SystemSettingsRepository;
import org.edu.service.SystemSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_TIME_ZONE = "Asia/Colombo";

    private final SystemSettingsRepository systemSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemSettingsDTO getSystemSettings() {
        return toDTO(getOrCreateSettings());
    }

    @Override
    public SystemSettingsDTO updateSystemSettings(SystemSettingsUpdateRequest request) {
        SystemSettings settings = getOrCreateSettings();
        apply(settings, request);
        validateTimes(settings);
        return toDTO(systemSettingsRepository.save(settings));
    }

    private SystemSettings getOrCreateSettings() {
        return systemSettingsRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> {
                    SystemSettings settings = new SystemSettings();
                    settings.setDefaultLanguage(DEFAULT_LANGUAGE);
                    settings.setTimeZone(DEFAULT_TIME_ZONE);
                    return systemSettingsRepository.save(settings);
                });
    }

    private void apply(SystemSettings settings, SystemSettingsUpdateRequest request) {
        if (request.getSchoolName() != null) {
            settings.setSchoolName(normalizeNullable(request.getSchoolName()));
        }
        if (request.getSchoolCode() != null) {
            settings.setSchoolCode(normalizeNullable(request.getSchoolCode()));
        }
        if (request.getAddress() != null) {
            settings.setAddress(normalizeNullable(request.getAddress()));
        }
        if (request.getPhoneNumber() != null) {
            settings.setPhoneNumber(normalizeNullable(request.getPhoneNumber()));
        }
        if (request.getEmail() != null) {
            settings.setEmail(normalizeNullable(request.getEmail(), true));
        }
        if (request.getPrincipalName() != null) {
            settings.setPrincipalName(normalizeNullable(request.getPrincipalName()));
        }
        if (request.getPrincipalTitle() != null) {
            settings.setPrincipalTitle(normalizeNullable(request.getPrincipalTitle()));
        }
        if (request.getSchoolStartTime() != null) {
            settings.setSchoolStartTime(request.getSchoolStartTime());
        }
        if (request.getSchoolEndTime() != null) {
            settings.setSchoolEndTime(request.getSchoolEndTime());
        }
        if (request.getAttendanceCutoffTime() != null) {
            settings.setAttendanceCutoffTime(request.getAttendanceCutoffTime());
        }
        if (request.getDefaultLanguage() != null) {
            settings.setDefaultLanguage(normalizeRequiredSetting(request.getDefaultLanguage(), "Default language"));
        }
        if (request.getTimeZone() != null) {
            settings.setTimeZone(normalizeRequiredSetting(request.getTimeZone(), "Time zone"));
        }
        if (request.getCurrentAcademicYearLabel() != null) {
            settings.setCurrentAcademicYearLabel(normalizeNullable(request.getCurrentAcademicYearLabel()));
        }
    }

    private void validateTimes(SystemSettings settings) {
        if (settings.getSchoolStartTime() != null
                && settings.getSchoolEndTime() != null
                && !settings.getSchoolStartTime().isBefore(settings.getSchoolEndTime())) {
            throw new IllegalArgumentException("School start time must be before school end time");
        }

        if (settings.getAttendanceCutoffTime() != null
                && settings.getSchoolStartTime() != null
                && settings.getAttendanceCutoffTime().isBefore(settings.getSchoolStartTime())) {
            throw new IllegalArgumentException("Attendance cutoff time must be after or equal to school start time");
        }

        if (settings.getAttendanceCutoffTime() != null
                && settings.getSchoolEndTime() != null
                && settings.getAttendanceCutoffTime().isAfter(settings.getSchoolEndTime())) {
            throw new IllegalArgumentException("Attendance cutoff time must be before or equal to school end time");
        }
    }

    private String normalizeNullable(String value) {
        return normalizeNullable(value, false);
    }

    private String normalizeNullable(String value, boolean lowerCase) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return lowerCase ? trimmed.toLowerCase() : trimmed;
    }

    private String normalizeRequiredSetting(String value, String label) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return normalized;
    }

    private SystemSettingsDTO toDTO(SystemSettings settings) {
        return new SystemSettingsDTO(
                settings.getId(),
                settings.getSchoolName(),
                settings.getSchoolCode(),
                settings.getAddress(),
                settings.getPhoneNumber(),
                settings.getEmail(),
                settings.getPrincipalName(),
                settings.getPrincipalTitle(),
                settings.getSchoolStartTime(),
                settings.getSchoolEndTime(),
                settings.getAttendanceCutoffTime(),
                settings.getDefaultLanguage(),
                settings.getTimeZone(),
                settings.getCurrentAcademicYearLabel(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}
