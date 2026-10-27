package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "training_class_id", nullable = false)
    private training_class trainingClass;

    private String title;
    private String description;
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private AssignmentType type;

    private String language;
    private Double maxScore;
    private Integer durationMinutes;

    @Version
    private Long version;

    protected Assignment() {
    }

    public Assignment(training_class trainingClass, String title, String description, LocalDateTime deadline) {
        this(trainingClass, title, description, deadline, AssignmentType.FILE, null, 10.0, null);
    }

    public Assignment(training_class trainingClass, String title, String description, LocalDateTime deadline,
            AssignmentType type, String language, Double maxScore, Integer durationMinutes) {
        this.trainingClass = trainingClass;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.type = type;
        this.language = language;
        this.maxScore = maxScore;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() { return id; }
    public training_class getTrainingClass() { return trainingClass; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDeadline() { return deadline; }
    public AssignmentType getType() { return type; }
    public String getLanguage() { return language; }
    public Double getMaxScore() { return maxScore; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Long getVersion() { return version; }
}