package org.edu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.edu.util.EmploymentType;
import org.edu.util.StaffCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Staff{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String staffId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_category", length = 30)
    private StaffCategory staffCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 30)
    private EmploymentType employmentType;

    @Column(length = 100)
    private String department;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "teaching_capable")
    private Boolean teachingCapable;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
