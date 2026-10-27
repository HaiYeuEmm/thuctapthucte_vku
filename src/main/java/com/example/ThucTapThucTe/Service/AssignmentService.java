package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.Assignment;
import com.example.ThucTapThucTe.Entity.AssignmentType;
import com.example.ThucTapThucTe.Repository.AssignmentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {
    private final AssignmentRepository repository;
    private final TrainingClassService classService;
    private final RealtimeUpdatePublisher realtimePublisher;

    public AssignmentService(AssignmentRepository repository, TrainingClassService classService, RealtimeUpdatePublisher realtimePublisher) {
        this.repository = repository;
        this.classService = classService;
        this.realtimePublisher = realtimePublisher;
    }

    public List<Assignment> findByClass(Long classId) { return repository.findByTrainingClassId(classId); }

    public Assignment create(Long classId, String title, String description, LocalDateTime deadline) {
        return create(classId, title, description, deadline, AssignmentType.FILE, null, null);
    }

    @Transactional
    public Assignment create(Long classId, String title, String description, LocalDateTime deadline,
            AssignmentType type, String language, Integer durationMinutes) {
        if (classId == null || title == null || title.isBlank() || deadline == null) {
            throw new IllegalArgumentException("Lop, tieu de va deadline la bat buoc");
        }
        if (type == null) throw new IllegalArgumentException("Loai bai tap la bat buoc");
        if ((type == AssignmentType.CODE || type == AssignmentType.QUIZ)
                && (durationMinutes == null || durationMinutes < 1 || durationMinutes > 300)) {
            throw new IllegalArgumentException("Thoi gian lam bai phai tu 1 den 300 phut");
        }
        if (type == AssignmentType.CODE && (language == null || language.isBlank())) throw new IllegalArgumentException("Bai code phai co ngon ngu");
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline phai o hien tai hoac tuong lai");
        }
        Assignment saved = repository.save(new Assignment(classService.findById(classId), title.trim(), description, deadline,
                type, language, 10.0, type == AssignmentType.FILE ? null : durationMinutes));
        realtimePublisher.publish("ASSIGNMENT_CREATED", classId, saved.getId(), saved.getId());
        return saved;
    }
}