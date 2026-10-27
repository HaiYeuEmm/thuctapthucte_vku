package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_sessions", uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "student_id"}))
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ExamSessionStatus status;

    @Column(nullable = false)
    private Integer violationCount = 0;

    @Column(length = 200)
    private String lastViolation;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ExamSession() { }

    public ExamSession(Assignment assignment, User student) {
        this.assignment = assignment;
        this.student = student;
        this.status = ExamSessionStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = this.startedAt;
    }

    public Long getId() { return id; }
    public Assignment getAssignment() { return assignment; }
    public User getStudent() { return student; }
    public ExamSessionStatus getStatus() { return status; }
    public Integer getViolationCount() { return violationCount; }
    public String getLastViolation() { return lastViolation; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void flag(String reason) {
        if (status == ExamSessionStatus.ACTIVE) {
            status = ExamSessionStatus.REVIEW_REQUIRED;
            violationCount = violationCount == null ? 1 : violationCount + 1;
            lastViolation = reason;
            updatedAt = LocalDateTime.now();
        }
    }

    public void decide(boolean resume) {
        status = resume ? ExamSessionStatus.ACTIVE : ExamSessionStatus.LOCKED;
        updatedAt = LocalDateTime.now();
    }

    public void submit() {
        status = ExamSessionStatus.SUBMITTED;
        updatedAt = LocalDateTime.now();
    }

    public void expire() {
        status = ExamSessionStatus.TIME_EXPIRED;
        updatedAt = LocalDateTime.now();
    }

    public void restart() {
        status = ExamSessionStatus.ACTIVE;
        violationCount = 0;
        lastViolation = null;
        startedAt = LocalDateTime.now();
        updatedAt = startedAt;
    }
}
