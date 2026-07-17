package org.edu.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.NoticeDTO;
import org.edu.dto.staffportal.StaffPortalProfileDTO;
import org.edu.entity.Staff;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.NoticeMapper;
import org.edu.repository.NoticeRepository;
import org.edu.repository.StaffRepository;
import org.edu.service.StaffPortalService;
import org.edu.util.EmploymentType;
import org.edu.util.NoticeAudience;
import org.edu.util.StaffCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StaffPortalServiceImpl implements StaffPortalService {

    private final StaffRepository staffRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeMapper noticeMapper;

    @Override
    public StaffPortalProfileDTO getProfile(Long authenticatedUserId) {
        return toProfile(getActiveStaff(authenticatedUserId));
    }

    @Override
    public List<NoticeDTO> getNotices(Long authenticatedUserId) {
        getActiveStaff(authenticatedUserId);
        return noticeRepository.findPortalNotices(NoticeAudience.STAFF, LocalDate.now())
                .stream()
                .map(noticeMapper::toDTO)
                .toList();
    }

    private Staff getActiveStaff(Long authenticatedUserId) {
        return staffRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active staff profile not found for current user"));
    }

    private StaffPortalProfileDTO toProfile(Staff staff) {
        StaffCategory category = staff.getStaffCategory() == null ? StaffCategory.SUPPORT : staff.getStaffCategory();
        EmploymentType employmentType = staff.getEmploymentType() == null
                ? EmploymentType.PERMANENT
                : staff.getEmploymentType();
        return new StaffPortalProfileDTO(
                staff.getId(),
                staff.getUser().getId(),
                staff.getStaffId(),
                staff.getName(),
                staff.getUser().getEmail(),
                staff.getPhoneNumber(),
                staff.getDesignation(),
                category,
                employmentType,
                staff.getDepartment(),
                staff.getJoiningDate(),
                staff.isActive(),
                staff.getCreatedAt(),
                staff.getUpdatedAt()
        );
    }
}
