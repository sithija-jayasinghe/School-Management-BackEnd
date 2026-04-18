package org.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "students")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE students SET active = false WHERE id = ?")
@SQLRestriction("active = true")
@NoArgsConstructor
@Getter
@Setter
public class Student {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        private User user;

        @Column(nullable = false, length = 150)
        private String name;

        @Column(nullable = false)
        private LocalDate dateOfBirth;

        @Column(nullable = false)
        private boolean active = true;

        @Column(nullable = false, length = 15)
        private String phoneNumber;

        @CreatedDate
        private LocalDateTime createdAt;

        @LastModifiedDate
        private LocalDateTime updatedAt;
}