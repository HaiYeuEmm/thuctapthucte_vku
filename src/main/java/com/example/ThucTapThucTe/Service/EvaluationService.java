package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.Evaluation;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.EvaluationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluationService {
    private final EvaluationRepository repository;
    private final TrainingClassService classService;
    private final usersservice usersService;
    private final RealtimeUpdatePublisher realtimePublisher;

    public EvaluationService(EvaluationRepository repository, TrainingClassService classService, usersservice usersService, RealtimeUpdatePublisher realtimePublisher) {
        this.repository = repository;
        this.classService = classService;
        this.usersService = usersService;
        this.realtimePublisher = realtimePublisher;
    }

    public List<Evaluation> findByClass(Long classId) { return repository.findByTrainingClassId(classId); }
    public List<Evaluation> findByStudent(Long studentId) { return repository.findByStudentId(studentId); }

    @Transactional
    public Evaluation create(Long classId, Long studentId, Double processScore, Double testScore,
            Double attendanceScore, Double finalScore, String feedback) {
        if (classId == null || studentId == null) throw new IllegalArgumentException("Lop va sinh vien la bat buoc");
        validateScore(processScore);
        validateScore(testScore);
        validateScore(attendanceScore);
        validateScore(finalScore);
        var trainingClass = classService.findById(classId);
        var student = usersService.requireRole(studentId, UserRole.STUDENT);
        if (trainingClass.getStudents().stream().noneMatch(item -> item.getId().equals(studentId))) {
            throw new IllegalArgumentException("Sinh vien khong thuoc lop thuc tap nay");
        }
        Evaluation evaluation = repository.findByTrainingClassIdAndStudentId(classId, studentId)
                .orElseGet(() -> new Evaluation(trainingClass, student, processScore, testScore,
                        attendanceScore, finalScore, feedback));
        evaluation.updateScores(processScore, testScore, attendanceScore, finalScore, feedback);
        Evaluation saved = repository.save(evaluation);
        realtimePublisher.publish("EVALUATION_UPDATED", classId, saved.getId(), saved.getId());
        return saved;
    }

    @Transactional
    public Evaluation approve(Long id) {
        Evaluation evaluation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay danh gia voi ma " + id));
        evaluation.approve();
        Evaluation saved = repository.save(evaluation);
        realtimePublisher.publish("EVALUATION_APPROVED", saved.getTrainingClass().getId(), saved.getId(), saved.getId());
        return saved;
    }

    private void validateScore(Double score) {
        if (score != null && (score < 0 || score > 10)) {
            throw new IllegalArgumentException("Diem phai nam trong khoang tu 0 den 10");
        }
    }
}
