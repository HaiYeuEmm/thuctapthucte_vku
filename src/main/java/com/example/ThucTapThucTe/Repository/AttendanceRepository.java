package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.Attendance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByTrainingClassId(Long trainingClassId);
    List<Attendance> findByStudentId(Long studentId);
    Optional<Attendance> findByTrainingClassIdAndStudentIdAndAttendanceDate(Long trainingClassId, Long studentId, java.time.LocalDate attendanceDate);
}
