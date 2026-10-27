package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.QuizQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByAssignmentId(Long assignmentId);
}