package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.Schedule;
import com.example.ThucTapThucTe.Entity.ScheduleType;
import com.example.ThucTapThucTe.Repository.ScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
    private final ScheduleRepository repository;
    private final TrainingClassService classService;
    private final RealtimeUpdatePublisher realtimePublisher;

    public ScheduleService(ScheduleRepository repository, TrainingClassService classService, RealtimeUpdatePublisher realtimePublisher) {
        this.repository = repository;
        this.classService = classService;
        this.realtimePublisher = realtimePublisher;
    }

    public List<Schedule> findByClass(Long classId) { return repository.findByTrainingClassId(classId); }

    @Transactional
    public Schedule create(Long classId, String title, LocalDateTime startsAt, LocalDateTime endsAt, ScheduleType type) {
        if (classId == null || title == null || title.isBlank() || startsAt == null || endsAt == null || type == null) {
            throw new IllegalArgumentException("Lop, tieu de, thoi gian va loai lich la bat buoc");
        }
        if (!endsAt.isAfter(startsAt)) throw new IllegalArgumentException("Thoi gian ket thuc phai sau thoi gian bat dau");
        if (endsAt.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Khong the tao lich da ket thuc");
        Schedule saved = repository.save(new Schedule(classService.findById(classId), title, startsAt, endsAt, type));
        realtimePublisher.publish("SCHEDULE_CREATED", classId, saved.getTrainingClass().getId(), saved.getId());
        return saved;
    }
}