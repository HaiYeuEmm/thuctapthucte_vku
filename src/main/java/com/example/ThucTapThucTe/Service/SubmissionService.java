package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.Assignment;
import com.example.ThucTapThucTe.Entity.AssignmentType;
import com.example.ThucTapThucTe.Entity.ExamSession;
import com.example.ThucTapThucTe.Entity.ExamSessionStatus;
import com.example.ThucTapThucTe.Entity.QuizQuestion;
import com.example.ThucTapThucTe.Entity.Submission;
import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.AssignmentRepository;
import com.example.ThucTapThucTe.Repository.ExamSessionRepository;
import com.example.ThucTapThucTe.Repository.QuizQuestionRepository;
import com.example.ThucTapThucTe.Repository.SubmissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {
    private final AssignmentRepository assignmentRepository;
    private final QuizQuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final ExamSessionRepository examSessionRepository;
    private final usersservice usersService;
    private final ObjectMapper objectMapper;
    private final Path uploadDirectory;
    private final RealtimeUpdatePublisher realtimePublisher;

    public SubmissionService(AssignmentRepository assignmentRepository, QuizQuestionRepository questionRepository,
            SubmissionRepository submissionRepository, ExamSessionRepository examSessionRepository,
            usersservice usersService, ObjectMapper objectMapper,
            @Value("${app.upload-dir:uploads}") String uploadDirectory, RealtimeUpdatePublisher realtimePublisher) {
        this.assignmentRepository = assignmentRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.examSessionRepository = examSessionRepository;
        this.usersService = usersService;
        this.objectMapper = objectMapper;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.realtimePublisher = realtimePublisher;
    }

    public List<Submission> findByAssignment(Long assignmentId) { return submissionRepository.findByAssignmentId(assignmentId); }
    public List<Submission> findByStudent(Long studentId) { return submissionRepository.findByStudentId(studentId); }
    public List<QuizQuestion> findQuestions(Long assignmentId) { return questionRepository.findByAssignmentId(assignmentId); }

    @Transactional
    public QuizQuestion addQuestion(Long assignmentId, String questionText, String optionA, String optionB,
            String optionC, String optionD, String correctOption, Double points) {
        return addQuestions(assignmentId, List.of(new QuizQuestionInput(questionText, optionA, optionB, optionC,
                optionD, correctOption, points))).get(0);
    }

    @Transactional
    public List<QuizQuestion> addQuestions(Long assignmentId, List<QuizQuestionInput> inputs) {
        if (inputs == null || inputs.isEmpty()) throw new IllegalArgumentException("Quiz phai co it nhat mot cau hoi");
        Assignment assignment = findAssignment(assignmentId);
        requireType(assignment, AssignmentType.QUIZ);
        List<QuizQuestion> questions = new ArrayList<>();
        for (QuizQuestionInput input : inputs) {
            if (input == null || isBlank(input.questionText()) || isBlank(input.optionA()) || isBlank(input.optionB())
                    || isBlank(input.optionC()) || isBlank(input.optionD())) {
                throw new IllegalArgumentException("Cau hoi va 4 phuong an la bat buoc");
            }
            if (input.correctOption() == null || !input.correctOption().matches("[ABCD]")) {
                throw new IllegalArgumentException("Dap an dung phai la A, B, C hoac D");
            }
            if (input.points() == null || input.points() <= 0) {
                throw new IllegalArgumentException("Diem cau hoi phai lon hon 0");
            }
            QuizQuestion question = questionRepository.save(new QuizQuestion(assignment, input.questionText(),
                    input.optionA(), input.optionB(), input.optionC(), input.optionD(), input.correctOption(),
                    input.points()));
            questions.add(question);
        }
        questions.forEach(question -> realtimePublisher.publish("QUIZ_QUESTION_CREATED",
                assignment.getTrainingClass().getId(), assignmentId, question.getId()));
        return questions;
    }

    public record QuizQuestionInput(String questionText, String optionA, String optionB, String optionC,
            String optionD, String correctOption, Double points) { }

    @Transactional
    public Submission submitFile(Long assignmentId, Long studentId, MultipartFile file) {
        Assignment assignment = findAssignment(assignmentId);
        requireType(assignment, AssignmentType.FILE);
        ensureBeforeDeadline(assignment);
        User student = requireStudentInAssignment(assignment, studentId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Phai chon file de nop");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("File khong duoc vuot qua 10 MB");
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "submission" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        String storedName = UUID.randomUUID() + (extension == null ? "" : "." + extension);
        try {
            Files.createDirectories(uploadDirectory);
            Files.write(uploadDirectory.resolve(storedName), file.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Khong the luu file nop");
        }
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
            .orElseGet(() -> new Submission(assignment, student));
        submission.resubmit();
        submission.setFileName(originalName);
        submission.setStoredPath(storedName);
        submission.setContentType(file.getContentType());
        Submission saved = submissionRepository.save(submission);
        realtimePublisher.publish("SUBMISSION_UPSERTED", assignment.getTrainingClass().getId(), saved.getId(), saved.getId());
        return saved;
    }

    @Transactional
    public Submission submitCode(Long assignmentId, Long studentId, String code) {
        Assignment assignment = findAssignment(assignmentId);
        requireType(assignment, AssignmentType.CODE);
        ensureBeforeDeadline(assignment);
        User student = requireStudentInAssignment(assignment, studentId);
        ExamSession examSession = requireActiveExamSession(assignmentId, studentId);
        ensureWithinExamDuration(examSession);
        if (isBlank(code)) throw new IllegalArgumentException("Ma nguon khong duoc de trong");
        if (code.length() > 200_000) throw new IllegalArgumentException("Ma nguon khong duoc vuot qua 200000 ky tu");
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
            .orElseGet(() -> new Submission(assignment, student));
        submission.resubmit();
        submission.setCode(code);
        submission.setLanguage(assignment.getLanguage());
        Submission saved = submissionRepository.save(submission);
        examSession.submit();
        examSessionRepository.save(examSession);
        realtimePublisher.publish("SUBMISSION_UPSERTED", assignment.getTrainingClass().getId(), saved.getId(), saved.getId());
        return saved;
    }

    @Transactional
    public Submission submitQuiz(Long assignmentId, Long studentId, Map<Long, String> answers) {
        Assignment assignment = findAssignment(assignmentId);
        requireType(assignment, AssignmentType.QUIZ);
        ensureBeforeDeadline(assignment);
        User student = requireStudentInAssignment(assignment, studentId);
        ExamSession examSession = requireActiveExamSession(assignmentId, studentId);
        ensureWithinExamDuration(examSession);
        List<QuizQuestion> questions = findQuestions(assignmentId);
        if (questions.isEmpty()) throw new IllegalArgumentException("Quiz chua co cau hoi");
        double total = questions.stream().mapToDouble(QuizQuestion::getPoints).sum();
        double earned = questions.stream().filter(question -> question.getCorrectOption().equalsIgnoreCase(answers.get(question.getId())))
                .mapToDouble(QuizQuestion::getPoints).sum();
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
            .orElseGet(() -> new Submission(assignment, student));
        submission.resubmit();
        try { submission.setQuizAnswers(objectMapper.writeValueAsString(answers)); }
        catch (JsonProcessingException exception) { throw new IllegalArgumentException("Dap an quiz khong hop le"); }
        submission.grade(Math.round((earned / total) * assignment.getMaxScore() * 100.0) / 100.0, "Quiz tu cham");
        Submission saved = submissionRepository.save(submission);
        examSession.submit();
        examSessionRepository.save(examSession);
        realtimePublisher.publish("QUIZ_GRADED", assignment.getTrainingClass().getId(), saved.getId(), saved.getScore());
        return saved;
    }

    @Transactional
    public Submission grade(Long submissionId, Long mentorId, Double score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay bai nop voi ma " + submissionId));
        User mentor = usersService.requireRole(mentorId, UserRole.MENTOR);
        if (submission.getAssignment().getTrainingClass().getMentor() == null
                || !submission.getAssignment().getTrainingClass().getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Mentor khong phu trach lop cua bai nop nay");
        }
        if (score == null || score < 0 || score > submission.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException("Diem phai nam trong khoang 0 den diem toi da cua bai");
        }
        submission.grade(score, feedback);
        Submission saved = submissionRepository.save(submission);
        realtimePublisher.publish("SUBMISSION_GRADED", saved.getAssignment().getTrainingClass().getId(), saved.getId(), saved.getScore());
        return saved;
    }

    private Assignment findAssignment(Long id) { return assignmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay bai tap voi ma " + id)); }
    private ExamSession requireActiveExamSession(Long assignmentId, Long studentId) {
        ExamSession session = examSessionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Hay bat dau bai kiem tra trong giao dien truoc khi nop"));
        if (session.getStatus() != ExamSessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Phien kiem tra dang bi khoa hoac da ket thuc");
        }
        return session;
    }
    private void ensureWithinExamDuration(ExamSession session) {
        Integer durationMinutes = session.getAssignment().getDurationMinutes();
        if (durationMinutes != null
                && !LocalDateTime.now().isBefore(session.getStartedAt().plusMinutes(durationMinutes))) {
            throw new IllegalArgumentException("Da het thoi gian lam bai");
        }
    }
    private void requireType(Assignment assignment, AssignmentType type) { if (assignment.getType() != type) throw new IllegalArgumentException("Bai tap khong phai dang " + type); }
    private void ensureBeforeDeadline(Assignment assignment) {
        if (assignment.getDeadline() != null
                && (assignment.getType() == AssignmentType.FILE || assignment.getDurationMinutes() == null)
                && LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new IllegalArgumentException("Da qua deadline, khong the nop bai");
        }
    }
    private User requireStudentInAssignment(Assignment assignment, Long studentId) {
        User student = usersService.requireRole(studentId, UserRole.STUDENT);
        if (assignment.getTrainingClass().getStudents().stream().noneMatch(item -> item.getId().equals(studentId))) throw new IllegalArgumentException("Sinh vien khong thuoc lop cua bai tap");
        return student;
    }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
