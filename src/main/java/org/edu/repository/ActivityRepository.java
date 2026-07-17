package org.edu.repository;

import java.util.Optional;
import org.edu.entity.Activity;
import org.edu.util.ActivityCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByIdAndActiveTrue(Long id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            select activity
            from Activity activity
            where activity.active = true
              and (:category is null or activity.category = :category)
              and (
                :keyword = ''
                or lower(activity.code) like lower(concat('%', :keyword, '%'))
                or lower(activity.name) like lower(concat('%', :keyword, '%'))
              )
            order by activity.name asc
            """)
    Page<Activity> searchActive(
            @Param("category") ActivityCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
