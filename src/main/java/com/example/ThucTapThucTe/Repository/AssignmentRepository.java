package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTrainingClassId(Long trainingClassId);
}