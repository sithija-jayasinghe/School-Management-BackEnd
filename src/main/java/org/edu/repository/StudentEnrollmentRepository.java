package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.StudentEnrollment;
import org.edu.util.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {

    Optional<StudentEnrollment> findByStudentIdAndAcademicYearId(Long studentId, Long academicYearId);

    Optional<StudentEnrollment> findByStudentIdAndAcademicYearIdAndStatus(
            Long studentId,
            Long academicYearId,
            EnrollmentStatus status
    );

    List<StudentEnrollment> findByStudentIdOrderByAcademicYearStartDateDesc(Long studentId);

    List<StudentEnrollment> findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
            Long studentId,
            EnrollmentStatus status
    );
}
