package org.edu.repository;

import org.edu.entity.Student;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByActiveTrue();

    Page<Student> findByActiveTrue(Pageable pageable);

    Page<Student> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByUser(User user);
}