package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.Attendance;
import com.example.ThucTapThucTe.Entity.AttendanceStatus;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.AttendanceRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private final AttendanceRepository repository;
    private final TrainingClassService classService;
    private final usersservice usersService;
    private final RealtimeUpdatePublisher realtimePublisher;

    public AttendanceService(AttendanceRepository repository, TrainingClassService classService, usersservice usersService, RealtimeUpdatePublisher realtimePublisher) {
        this.repository = repository;
        this.classService = classService;
        this.usersService = usersService;
        this.realtimePublisher = realtimePublisher;
    }

    public List<Attendance> findByClass(Long classId) { return repository.findByTrainingClassId(classId); }
    public List<Attendance> findByStudent(Long studentId) { return repository.findByStudentId(studentId); }

    @Transactional
    public Attendance create(Long classId, Long studentId, LocalDate date, AttendanceStatus status) {
        if (classId == null || studentId == null || date == null || status == null) {
            throw new IllegalArgumentException("Lop, sinh vien, ngay diem danh va trang thai la bat buoc");
        }
        if (date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Khong the diem danh cho ngay tuong lai");
        var trainingClass = classService.findById(classId);
        var student = usersService.requireRole(studentId, UserRole.STUDENT);
        if (trainingClass.getStudents().stream().noneMatch(item -> item.getId().equals(studentId))) {
            throw new IllegalArgumentException("Sinh vien khong thuoc lop thuc tap nay");
        }
        Attendance attendance = repository.findByTrainingClassIdAndStudentIdAndAttendanceDate(classId, studentId, date)
                .orElseGet(() -> new Attendance(trainingClass, student, date, status));
        attendance.updateStatus(status);
        Attendance saved = repository.save(attendance);
        realtimePublisher.publish("ATTENDANCE_RECORDED", classId, saved.getId(), saved.getId());
        return saved;
    }
}
