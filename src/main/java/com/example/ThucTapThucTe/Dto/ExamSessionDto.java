package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.ExamSession;
import com.example.ThucTapThucTe.Entity.ExamSessionStatus;
import java.time.LocalDateTime;

public record ExamSessionDto(Long id, Long assignmentId, String assignmentTitle, Long classId, String className,
        Long studentId, String studentName, ExamSessionStatus status, Integer violationCount,
        String lastViolation, LocalDateTime startedAt, LocalDateTime updatedAt, LocalDateTime timeLimitEndsAt) {
    public static ExamSessionDto from(ExamSession session) {
        var assignment = session.getAssignment();
        var trainingClass = assignment.getTrainingClass();
        return new ExamSessionDto(session.getId(), assignment.getId(), assignment.getTitle(), trainingClass.getId(),
                trainingClass.getName(), session.getStudent().getId(), session.getStudent().getFullName(),
                session.getStatus(), session.getViolationCount(), session.getLastViolation(),
                session.getStartedAt(), session.getUpdatedAt(), session.getAssignment().getDurationMinutes() == null
                        ? null : session.getStartedAt().plusMinutes(session.getAssignment().getDurationMinutes()));
    }
}
