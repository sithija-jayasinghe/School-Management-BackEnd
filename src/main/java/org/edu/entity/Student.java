package org.edu.entity;

import jakarta.persistence.*;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
public class Student {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 150)
        private String name;

        @Column(length = 60, unique = true)
        private String admissionNumber;

        @Column(length = 150)
        private String nameWithInitials;

        @Column(nullable = false)
        private LocalDate dateOfBirth;

        @Column(length = 20)
        private String gender;

        private LocalDate admissionDate;

        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private org.edu.util.SchoolMedium medium;

        @Column(length = 500)
        private String homeAddress;

        @Column(length = 50)
        private String guardianRelationship;

        @Column(length = 500)
        private String medicalConditions;

        @Column(length = 150)
        private String previousSchool;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private org.edu.util.StudentStatus status = org.edu.util.StudentStatus.ACTIVE;

        @Column(nullable = false)
        private boolean active = true;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "class_id")
        private org.edu.entity.Class currentClass;

        @Column(length = 50)
        private String house;

        @Column(name = "phone_number", nullable = false, length = 20)
        private String legacyPhoneNumber = "";

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "house_id")
        private House assignedHouse;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "student_activities",
                joinColumns = @JoinColumn(name = "student_id"),
                inverseJoinColumns = @JoinColumn(name = "activity_id")
        )
        private Set<Activity> activities = new LinkedHashSet<>();

        @CreatedDate
        private LocalDateTime createdAt;

        @LastModifiedDate
        private LocalDateTime updatedAt;

        @PrePersist
        @PreUpdate
        private void applyLegacyColumnDefaults() {
                if (legacyPhoneNumber == null) {
                        legacyPhoneNumber = "";
                }
        }
}
