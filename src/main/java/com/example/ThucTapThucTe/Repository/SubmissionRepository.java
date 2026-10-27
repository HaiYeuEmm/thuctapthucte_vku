package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByStudentId(Long studentId);
    java.util.Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}