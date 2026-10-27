package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTrainingClassId(Long trainingClassId);
}