package org.edu.service;

import java.util.List;
import org.edu.dto.NoticeDTO;
import org.edu.dto.staffportal.StaffPortalProfileDTO;

public interface StaffPortalService {

    StaffPortalProfileDTO getProfile(Long authenticatedUserId);

    List<NoticeDTO> getNotices(Long authenticatedUserId);
}
