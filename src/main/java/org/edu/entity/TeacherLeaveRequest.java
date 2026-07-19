package org.edu.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.TeacherLeaveDuration;
import org.edu.util.TeacherLeaveStatus;
import org.edu.util.TeacherLeaveType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "teacher_leave_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class TeacherLeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_staff_id", nullable = false)
    private Staff teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private TeacherLeaveType leaveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false, length = 20)
    private TeacherLeaveDuration durationType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherLeaveStatus status = TeacherLeaveStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewer_remarks", length = 1000)
    private String reviewerRemarks;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sessionDate ASC, startTime ASC")
    private List<TeacherLeaveSession> affectedSessions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void replaceAffectedSessions(List<TeacherLeaveSession> sessions) {
        affectedSessions.clear();
        sessions.forEach(session -> {
            session.setLeaveRequest(this);
            affectedSessions.add(session);
        });
    }
}
