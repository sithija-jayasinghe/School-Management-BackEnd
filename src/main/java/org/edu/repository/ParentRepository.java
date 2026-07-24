package org.edu.repository;

import org.edu.entity.Parent;
import org.edu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByIdAndActiveTrue(Long id);

    Optional<Parent> findByPhoneNumber(String phoneNumber);

    Page<Parent> findByActiveTrue(Pageable pageable);

    List<Parent> findByActiveTrue();
    
    List<Parent> findByActiveFalse();

    @Query("""
            select parent
            from Parent parent
            where parent.active = true
              and (
                lower(parent.name) like lower(concat('%', :keyword, '%'))
                or parent.phoneNumber like concat('%', :keyword, '%')
              )
            """)
    Page<Parent> searchActiveParents(@Param("keyword") String keyword, Pageable pageable);

    Optional<Parent> findByUser_Id(Long userId);

    Optional<Parent> findByUser_IdAndActiveTrue(Long userId);

    boolean existsByUser(User user);
}
