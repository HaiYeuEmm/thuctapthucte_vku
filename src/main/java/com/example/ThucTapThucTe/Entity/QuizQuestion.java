package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    private String questionText;
    @Column(name = "option_a", nullable = false, length = 1000)
    private String optionA;
    @Column(name = "option_b", nullable = false, length = 1000)
    private String optionB;
    @Column(name = "option_c", nullable = false, length = 1000)
    private String optionC;
    @Column(name = "option_d", nullable = false, length = 1000)
    private String optionD;
    private String correctOption;
    private Double points;

    protected QuizQuestion() { }

    public QuizQuestion(Assignment assignment, String questionText, String optionA, String optionB,
            String optionC, String optionD, String correctOption, Double points) {
        this.assignment = assignment;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.points = points;
    }

    public Long getId() { return id; }
    public Assignment getAssignment() { return assignment; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
    public Double getPoints() { return points; }
}
