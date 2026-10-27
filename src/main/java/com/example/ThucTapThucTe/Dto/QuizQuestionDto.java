package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.QuizQuestion;

public record QuizQuestionDto(Long id, Long assignmentId, String questionText, String optionA,
        String optionB, String optionC, String optionD, Double points) {
    public static QuizQuestionDto from(QuizQuestion question) {
        return new QuizQuestionDto(question.getId(), question.getAssignment().getId(), question.getQuestionText(),
                question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD(), question.getPoints());
    }
}