package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.ActivityTeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityTeacherAssignmentRepository extends JpaRepository<ActivityTeacherAssignment, Long> {

    List<ActivityTeacherAssignment> findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(Long activityId);

    Optional<ActivityTeacherAssignment> findByIdAndActivityIdAndActiveTrue(Long id, Long activityId);

    Optional<ActivityTeacherAssignment> findByActivityIdAndAcademicYearIdAndPrimaryResponsibleTrueAndActiveTrue(
            Long activityId,
            Long academicYearId
    );

    Optional<ActivityTeacherAssignment> findByActivityIdAndStaffIdAndAcademicYearId(
            Long activityId,
            Long staffId,
            Long academicYearId
    );

    long countByActivityIdAndActiveTrue(Long activityId);

    long countByActivityIdAndAcademicYearIdAndActiveTrue(Long activityId, Long academicYearId);
}
