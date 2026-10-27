package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.Assignment;
import com.example.ThucTapThucTe.Entity.AssignmentType;
import java.time.LocalDateTime;

public record AssignmentDto(Long id, Long classId, String className, String title,
    String description, LocalDateTime deadline, AssignmentType type, String language, Double maxScore,
    Integer durationMinutes) {

    public static AssignmentDto from(Assignment assignment) {
        return new AssignmentDto(assignment.getId(), assignment.getTrainingClass().getId(),
                assignment.getTrainingClass().getName(), assignment.getTitle(), assignment.getDescription(),
                assignment.getDeadline(), assignment.getType(), assignment.getLanguage(), assignment.getMaxScore(),
                assignment.getDurationMinutes());
    }
}