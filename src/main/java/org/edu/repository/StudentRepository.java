package org.edu.repository;

import org.edu.entity.Student;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByIdAndActiveTrue(Long id);

    Page<Student> findByActiveTrue(Pageable pageable);

    List<Student> findByActiveTrue();

    List<Student> findByCurrentClassIdAndActiveTrueOrderByNameAsc(Long classId);

    Page<Student> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByUser(User user);
}
