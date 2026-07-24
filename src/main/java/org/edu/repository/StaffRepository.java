package org.edu.repository;

import org.edu.entity.Staff;
import org.edu.entity.User;
import org.edu.util.EmploymentType;
import org.edu.util.Role;
import org.edu.util.StaffCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByIdAndActiveTrue(Long id);

    Optional<Staff> findByUser_IdAndActiveTrue(Long userId);

    Page<Staff> findByActiveTrue(Pageable pageable);

    List<Staff> findByActiveTrue();

    Page<Staff> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByUser(User user);

    @Query("""
            select staff
            from Staff staff
            left join staff.user user
            where (:keyword is null
                    or lower(staff.name) like lower(concat('%', :keyword, '%'))
                    or lower(staff.staffId) like lower(concat('%', :keyword, '%'))
                    or lower(staff.designation) like lower(concat('%', :keyword, '%'))
                    or lower(staff.department) like lower(concat('%', :keyword, '%'))
                    or staff.phoneNumber like concat('%', :keyword, '%'))
              and (:category is null or staff.staffCategory = :category)
              and (:employmentType is null or staff.employmentType = :employmentType)
              and (:department is null or lower(staff.department) like lower(concat('%', :department, '%')))
              and (:teachingCapable is null or staff.teachingCapable = :teachingCapable)
              and (:role is null or user.role = :role)
              and (:active is null or staff.active = :active)
            """)
    Page<Staff> filterStaff(
            @Param("keyword") String keyword,
            @Param("category") StaffCategory category,
            @Param("employmentType") EmploymentType employmentType,
            @Param("department") String department,
            @Param("teachingCapable") Boolean teachingCapable,
            @Param("role") Role role,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
