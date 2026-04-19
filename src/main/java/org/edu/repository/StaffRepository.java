package org.edu.repository;

import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Page<Staff> findByActiveTrue(Pageable pageable);

    List<Staff> findByActiveTrue();

    Page<Staff> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByUser(User user);
}
