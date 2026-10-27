package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submissions", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"assignment_id", "student_id"}))
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private String fileName;
    private String storedPath;
    private String contentType;
    private String language;
    private LocalDateTime submittedAt;

    @Lob
    private String code;

    @Lob
    private String quizAnswers;

    private Double score;
    private String feedback;

    @Version
    private Long version;

    protected Submission() { }

    public Submission(Assignment assignment, User student) {
        this.assignment = assignment;
        this.student = student;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Assignment getAssignment() { return assignment; }
    public User getStudent() { return student; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getQuizAnswers() { return quizAnswers; }
    public void setQuizAnswers(String quizAnswers) { this.quizAnswers = quizAnswers; }
    public Double getScore() { return score; }
    public String getFeedback() { return feedback; }
    public Long getVersion() { return version; }
    public void resubmit() { this.submittedAt = LocalDateTime.now(); this.score = null; this.feedback = null; }
    public void grade(Double score, String feedback) { this.score = score; this.feedback = feedback; }
}