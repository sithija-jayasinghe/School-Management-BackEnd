package org.edu.repository;

import org.edu.entity.Student;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByIdAndActiveTrue(Long id);

    Page<Student> findByActiveTrue(Pageable pageable);

    List<Student> findByActiveTrue();

    List<Student> findByCurrentClassIdAndActiveTrueOrderByNameAsc(Long classId);

    Page<Student> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    @Query("""
            select student
            from Student student
            left join student.currentClass studentClass
            where (:active is null or student.active = :active)
              and (:classId is null or studentClass.id = :classId)
              and (
                :keyword is null
                or lower(student.name) like lower(concat('%', :keyword, '%'))
                or lower(student.phoneNumber) like lower(concat('%', :keyword, '%'))
                or str(student.id) like concat('%', :keyword, '%')
              )
            """)
    Page<Student> filterStudents(
            @Param("keyword") String keyword,
            @Param("classId") Long classId,
            @Param("active") Boolean active,
            Pageable pageable
    );

    boolean existsByUser(User user);
}
