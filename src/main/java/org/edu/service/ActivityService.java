package org.edu.service;

import java.util.List;
import org.edu.dto.activity.ActivityDTO;
import org.edu.dto.activity.ActivitySaveRequest;
import org.edu.dto.activity.ActivityTeacherAssignmentDTO;
import org.edu.dto.activity.ActivityTeacherAssignmentSaveRequest;
import org.edu.util.ActivityCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityService {

    ActivityDTO createActivity(ActivitySaveRequest request);

    ActivityDTO updateActivity(Long activityId, ActivitySaveRequest request);

    void deactivateActivity(Long activityId);

    ActivityDTO getActivity(Long activityId);

    Page<ActivityDTO> getActivities(ActivityCategory category, String keyword, Pageable pageable);

    ActivityTeacherAssignmentDTO assignTeacher(Long activityId, ActivityTeacherAssignmentSaveRequest request);

    ActivityTeacherAssignmentDTO updateTeacherAssignment(
            Long activityId,
            Long assignmentId,
            ActivityTeacherAssignmentSaveRequest request
    );

    void deactivateTeacherAssignment(Long activityId, Long assignmentId);

    List<ActivityTeacherAssignmentDTO> getTeacherAssignments(Long activityId);
}
