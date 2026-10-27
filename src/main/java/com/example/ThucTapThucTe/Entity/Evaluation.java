package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "training_class_id", nullable = false)
    private training_class trainingClass;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private Double processScore;
    private Double testScore;
    private Double attendanceScore;
    private Double finalScore;
    private String feedback;
    private boolean approved;

    protected Evaluation() {
    }

    public Evaluation(training_class trainingClass, User student, Double processScore,
            Double testScore, Double attendanceScore, Double finalScore, String feedback) {
        this.trainingClass = trainingClass;
        this.student = student;
        this.processScore = processScore;
        this.testScore = testScore;
        this.attendanceScore = attendanceScore;
        this.finalScore = finalScore;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public training_class getTrainingClass() { return trainingClass; }
    public User getStudent() { return student; }
    public Double getProcessScore() { return processScore; }
    public Double getTestScore() { return testScore; }
    public Double getAttendanceScore() { return attendanceScore; }
    public Double getFinalScore() { return finalScore; }
    public String getFeedback() { return feedback; }
    public boolean isApproved() { return approved; }
    public void approve() { this.approved = true; }
    public void updateScores(Double processScore, Double testScore, Double attendanceScore, Double finalScore, String feedback) {
        this.processScore = processScore;
        this.testScore = testScore;
        this.attendanceScore = attendanceScore;
        this.finalScore = finalScore;
        this.feedback = feedback;
        this.approved = false;
    }
}
