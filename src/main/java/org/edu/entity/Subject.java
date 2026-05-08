package org.edu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Table (name = "subjects")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 5)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column (nullable = false)
    private String description;

    @ManyToMany(mappedBy = "subjects")
    private List<Class> classes;
}
