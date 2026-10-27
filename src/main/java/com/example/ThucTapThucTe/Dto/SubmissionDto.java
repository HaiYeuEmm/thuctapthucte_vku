package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.Submission;
import java.time.LocalDateTime;

public record SubmissionDto(Long id, Long assignmentId, String assignmentTitle, Long studentId,
        String studentName, String fileName, String language, String code, LocalDateTime submittedAt,
        Double score, String feedback) {
    public static SubmissionDto from(Submission submission) {
        return new SubmissionDto(submission.getId(), submission.getAssignment().getId(),
                submission.getAssignment().getTitle(), submission.getStudent().getId(), submission.getStudent().getFullName(),
                submission.getFileName(), submission.getLanguage(), submission.getCode(), submission.getSubmittedAt(),
                submission.getScore(), submission.getFeedback());
    }
}