package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.StudentMark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {

    Optional<StudentMark> findByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamIdAndStudentIdAndIdNot(Long examId, Long studentId, Long id);

    Page<StudentMark> findByExamIdOrderByStudentNameAsc(Long examId, Pageable pageable);

    Page<StudentMark> findByStudentIdOrderByExamExamDateDesc(Long studentId, Pageable pageable);

    List<StudentMark> findByExamId(Long examId);
}
