package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Dto.ExamSessionDto;
import com.example.ThucTapThucTe.Entity.AssignmentType;
import com.example.ThucTapThucTe.Entity.ExamSession;
import com.example.ThucTapThucTe.Entity.ExamSessionStatus;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.AssignmentRepository;
import com.example.ThucTapThucTe.Repository.ExamSessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamSessionService {
    private static final List<ExamSessionStatus> ACTIVE_STUDENT_STATES = List.of(
            ExamSessionStatus.ACTIVE, ExamSessionStatus.REVIEW_REQUIRED, ExamSessionStatus.LOCKED);

    private final ExamSessionRepository repository;
    private final AssignmentRepository assignmentRepository;
    private final usersservice usersService;
    private final TrainingClassService classService;
    private final RealtimeUpdatePublisher realtimePublisher;

    public ExamSessionService(ExamSessionRepository repository, AssignmentRepository assignmentRepository,
            usersservice usersService, TrainingClassService classService, RealtimeUpdatePublisher realtimePublisher) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
        this.usersService = usersService;
        this.classService = classService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public ExamSessionDto start(Long assignmentId, Long studentId) {
        if (assignmentId == null || studentId == null) throw new IllegalArgumentException("Bai kiem tra va sinh vien la bat buoc");
        var assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay bai kiem tra"));
        if (assignment.getType() != AssignmentType.CODE && assignment.getType() != AssignmentType.QUIZ) {
            throw new IllegalArgumentException("Chi bai thi trac nghiem va code moi can mo phien kiem tra");
        }
        if (assignment.getDeadline() != null && LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new IllegalArgumentException("Da qua han, khong the bat dau bai kiem tra");
        }
        var student = usersService.requireRole(studentId, UserRole.STUDENT);
        if (assignment.getTrainingClass().getStudents().stream().noneMatch(user -> user.getId().equals(studentId))) {
            throw new IllegalArgumentException("Sinh vien khong thuoc lop cua bai kiem tra");
        }
        Optional<ExamSession> existing = repository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        if (existing.isPresent()) {
            ExamSession session = existing.get();
            if (ACTIVE_STUDENT_STATES.contains(session.getStatus()) && hasTimeExpired(session)) {
                session.expire();
                return ExamSessionDto.from(repository.save(session));
            }
            if (session.getStatus() == ExamSessionStatus.REVIEW_REQUIRED) throw new IllegalArgumentException("Phien thi dang bi tam khoa, cho mentor duyet");
            if (session.getStatus() == ExamSessionStatus.LOCKED) throw new IllegalArgumentException("Mentor da khoa phien thi nay");
            if (session.getStatus() == ExamSessionStatus.TIME_EXPIRED) {
                throw new IllegalArgumentException("Da het thoi gian lam bai, khong the bat dau lai");
            }
            if (session.getStatus() == ExamSessionStatus.SUBMITTED) {
                if (assignment.getType() != AssignmentType.QUIZ) {
                    throw new IllegalArgumentException("Bai kiem tra da duoc nop");
                }
                if (repository.findFirstByStudentIdAndStatusInOrderByUpdatedAtDesc(studentId, ACTIVE_STUDENT_STATES).isPresent()) {
                    throw new IllegalArgumentException("Ban dang co mot phien kiem tra chua ket thuc");
                }
                session.restart();
                return ExamSessionDto.from(repository.save(session));
            }
            return ExamSessionDto.from(session);
        }
        if (repository.findFirstByStudentIdAndStatusInOrderByUpdatedAtDesc(studentId, ACTIVE_STUDENT_STATES).isPresent()) {
            throw new IllegalArgumentException("Ban dang co mot phien kiem tra chua ket thuc");
        }
        return ExamSessionDto.from(repository.save(new ExamSession(assignment, student)));
    }

    @Transactional
    public ExamSessionDto flag(Long sessionId, Long studentId, String reason) {
        ExamSession session = requireStudentSession(sessionId, studentId);
        if (expireIfTimeLimitReached(session)) return ExamSessionDto.from(session);
        if (reason == null || reason.isBlank()) reason = "Roi khoi man hinh kiem tra";
        if (reason.length() > 200) reason = reason.substring(0, 200);
        session.flag(reason);
        ExamSession saved = repository.save(session);
        if (saved.getStatus() == ExamSessionStatus.REVIEW_REQUIRED) {
            ExamSessionDto update = ExamSessionDto.from(saved);
            Long classId = saved.getAssignment().getTrainingClass().getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    realtimePublisher.publish("EXAM_SECURITY_FLAGGED", classId, saved.getId(), update);
                }
            });
        }
        return ExamSessionDto.from(saved);
    }

    @Transactional
    public Optional<ExamSessionDto> activeForStudent(Long studentId) {
        usersService.requireRole(studentId, UserRole.STUDENT);
        return repository.findFirstByStudentIdAndStatusInOrderByUpdatedAtDesc(studentId, ACTIVE_STUDENT_STATES)
                .map(session -> expireIfTimeLimitReached(session) ? null : ExamSessionDto.from(session));
    }

    @Transactional
    public ExamSessionDto getForStudent(Long sessionId, Long studentId) {
        ExamSession session = requireStudentSession(sessionId, studentId);
        expireIfTimeLimitReached(session);
        return ExamSessionDto.from(session);
    }

    public List<ExamSessionDto> findForClass(Long classId, Long mentorId) {
        var trainingClass = classService.findById(classId);
        var mentor = usersService.requireRole(mentorId, UserRole.MENTOR);
        if (trainingClass.getMentor() == null || !trainingClass.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Mentor khong phu trach lop nay");
        }
        return repository.findByAssignmentTrainingClassIdAndStatusInOrderByUpdatedAtDesc(classId,
                List.of(ExamSessionStatus.REVIEW_REQUIRED, ExamSessionStatus.LOCKED))
                .stream().filter(session -> !expireIfTimeLimitReached(session)).map(ExamSessionDto::from).toList();
    }

    @Transactional
    public ExamSessionDto decide(Long sessionId, Long mentorId, String decision) {
        ExamSession session = repository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phien kiem tra"));
        var trainingClass = session.getAssignment().getTrainingClass();
        var mentor = usersService.requireRole(mentorId, UserRole.MENTOR);
        if (trainingClass.getMentor() == null || !trainingClass.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Mentor khong phu trach lop cua sinh vien nay");
        }
        if (session.getStatus() != ExamSessionStatus.REVIEW_REQUIRED && session.getStatus() != ExamSessionStatus.LOCKED) {
            throw new IllegalArgumentException("Phien thi nay khong cho phep thay doi trang thai");
        }
        if (expireIfTimeLimitReached(session)) return ExamSessionDto.from(session);
        if (!"RESUME".equals(decision) && !"LOCK".equals(decision)) {
            throw new IllegalArgumentException("Quyet dinh phai la RESUME hoac LOCK");
        }
        session.decide("RESUME".equals(decision));
        ExamSession saved = repository.save(session);
        realtimePublisher.publish("EXAM_SECURITY_DECISION", trainingClass.getId(), saved.getId(), ExamSessionDto.from(saved));
        return ExamSessionDto.from(saved);
    }

    @Transactional
    public void complete(Long assignmentId, Long studentId) {
        ExamSession session = repository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phien kiem tra"));
        if (expireIfTimeLimitReached(session)) return;
        if (session.getStatus() != ExamSessionStatus.ACTIVE) throw new IllegalArgumentException("Phien kiem tra dang bi khoa hoac da ket thuc");
        session.submit();
        repository.save(session);
    }

    private boolean hasTimeExpired(ExamSession session) {
        Integer durationMinutes = session.getAssignment().getDurationMinutes();
        return durationMinutes != null
                && !LocalDateTime.now().isBefore(session.getStartedAt().plusMinutes(durationMinutes));
    }

    private boolean expireIfTimeLimitReached(ExamSession session) {
        if (!ACTIVE_STUDENT_STATES.contains(session.getStatus()) || !hasTimeExpired(session)) return false;
        if (session.getStatus() != ExamSessionStatus.TIME_EXPIRED) {
            session.expire();
            repository.save(session);
        }
        return true;
    }

    private ExamSession requireStudentSession(Long sessionId, Long studentId) {
        ExamSession session = repository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phien kiem tra"));
        if (!session.getStudent().getId().equals(studentId)) throw new IllegalArgumentException("Phien kiem tra khong thuoc sinh vien nay");
        return session;
    }
}
