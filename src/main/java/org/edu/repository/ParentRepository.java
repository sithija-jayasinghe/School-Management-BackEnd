package org.edu.repository;

import org.edu.entity.Parent;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByIdAndActiveTrue(Long id);

    Page<Parent> findByActiveTrue(Pageable pageable);

    List<Parent> findByActiveTrue();
    
    List<Parent> findByActiveFalse();

    Page<Parent> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Optional<Parent> findByUser_Id(Long userId);

    boolean existsByUser(User user);
}
