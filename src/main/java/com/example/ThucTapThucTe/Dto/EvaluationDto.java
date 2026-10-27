package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.Evaluation;

public record EvaluationDto(Long id, Long classId, Long studentId, String studentName,
        Double processScore, Double testScore, Double attendanceScore, Double finalScore,
        String feedback, boolean approved) {

    public static EvaluationDto from(Evaluation evaluation) {
        return new EvaluationDto(evaluation.getId(), evaluation.getTrainingClass().getId(),
                evaluation.getStudent().getId(), evaluation.getStudent().getFullName(),
                evaluation.getProcessScore(), evaluation.getTestScore(), evaluation.getAttendanceScore(),
                evaluation.getFinalScore(), evaluation.getFeedback(), evaluation.isApproved());
    }
}