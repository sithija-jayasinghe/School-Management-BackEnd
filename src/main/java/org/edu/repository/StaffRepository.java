package org.edu.repository;

import org.edu.entity.Staff;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByIdAndActiveTrue(Long id);

    Optional<Staff> findByUser_IdAndActiveTrue(Long userId);

    Page<Staff> findByActiveTrue(Pageable pageable);

    List<Staff> findByActiveTrue();

    Page<Staff> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByUser(User user);
}
