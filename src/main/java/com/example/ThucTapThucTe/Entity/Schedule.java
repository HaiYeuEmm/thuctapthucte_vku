package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "training_class_id", nullable = false)
    private training_class trainingClass;

    private String title;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    protected Schedule() {
    }

    public Schedule(training_class trainingClass, String title, LocalDateTime startsAt,
            LocalDateTime endsAt, ScheduleType type) {
        this.trainingClass = trainingClass;
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.type = type;
    }

    public Long getId() { return id; }
    public training_class getTrainingClass() { return trainingClass; }
    public String getTitle() { return title; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public ScheduleType getType() { return type; }
}