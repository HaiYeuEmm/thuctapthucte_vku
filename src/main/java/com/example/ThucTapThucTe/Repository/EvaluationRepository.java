package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.Evaluation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByTrainingClassId(Long trainingClassId);
    List<Evaluation> findByStudentId(Long studentId);
    Optional<Evaluation> findByTrainingClassIdAndStudentId(Long trainingClassId, Long studentId);
}
