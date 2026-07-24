package org.edu.service;

import java.util.List;
import org.edu.dto.teacherleave.SubstituteTeacherOptionDTO;
import org.edu.dto.teacherleave.TeacherLeaveCoverageRequest;
import org.edu.dto.teacherleave.TeacherLeaveRequestDTO;
import org.edu.dto.teacherleave.TeacherLeaveReviewRequest;
import org.edu.dto.teacherleave.TeacherLeaveSaveRequest;
import org.edu.dto.teacherleave.TeacherLeaveSessionDTO;
import org.edu.util.TeacherLeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherLeaveService {

    TeacherLeaveRequestDTO submit(Long authenticatedUserId, TeacherLeaveSaveRequest request);

    TeacherLeaveRequestDTO updateOwnRequest(Long authenticatedUserId, Long requestId, TeacherLeaveSaveRequest request);

    TeacherLeaveRequestDTO cancelOwnRequest(Long authenticatedUserId, Long requestId);

    TeacherLeaveRequestDTO getOwnRequest(Long authenticatedUserId, Long requestId);

    Page<TeacherLeaveRequestDTO> getOwnRequests(Long authenticatedUserId, TeacherLeaveStatus status, Pageable pageable);

    Page<TeacherLeaveRequestDTO> getAllRequests(TeacherLeaveStatus status, Pageable pageable);

    TeacherLeaveRequestDTO getRequest(Long requestId);

    List<TeacherLeaveSessionDTO> getAffectedSessions(Long requestId);

    List<SubstituteTeacherOptionDTO> getSubstituteOptions(Long sessionId);

    TeacherLeaveSessionDTO updateCoverage(Long sessionId, TeacherLeaveCoverageRequest request);

    TeacherLeaveRequestDTO approve(Long authenticatedUserId, Long requestId, TeacherLeaveReviewRequest request);

    TeacherLeaveRequestDTO reject(Long authenticatedUserId, Long requestId, TeacherLeaveReviewRequest request);
}
