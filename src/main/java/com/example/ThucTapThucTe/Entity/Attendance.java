package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "training_class_id", nullable = false)
    private training_class trainingClass;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    protected Attendance() {
    }

    public Attendance(training_class trainingClass, User student, LocalDate attendanceDate,
            AttendanceStatus status) {
        this.trainingClass = trainingClass;
        this.student = student;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public training_class getTrainingClass() { return trainingClass; }
    public User getStudent() { return student; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public AttendanceStatus getStatus() { return status; }
    public void updateStatus(AttendanceStatus status) { this.status = status; }
}
