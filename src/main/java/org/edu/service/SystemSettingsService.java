package org.edu.service;

import org.edu.dto.SystemSettingsDTO;
import org.edu.dto.request.SystemSettingsUpdateRequest;

public interface SystemSettingsService {

    SystemSettingsDTO getSystemSettings();

    SystemSettingsDTO updateSystemSettings(SystemSettingsUpdateRequest request);
}
