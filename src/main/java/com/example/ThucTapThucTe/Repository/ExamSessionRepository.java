package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.ExamSession;
import com.example.ThucTapThucTe.Entity.ExamSessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    Optional<ExamSession> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    Optional<ExamSession> findFirstByStudentIdAndStatusInOrderByUpdatedAtDesc(Long studentId, List<ExamSessionStatus> statuses);
    List<ExamSession> findByAssignmentTrainingClassIdAndStatusInOrderByUpdatedAtDesc(Long classId, List<ExamSessionStatus> statuses);
}
