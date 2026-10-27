package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.Schedule;
import com.example.ThucTapThucTe.Entity.ScheduleType;
import java.time.LocalDateTime;

public record ScheduleDto(Long id, Long classId, String className, String title,
        LocalDateTime startsAt, LocalDateTime endsAt, ScheduleType type) {

    public static ScheduleDto from(Schedule schedule) {
        return new ScheduleDto(schedule.getId(), schedule.getTrainingClass().getId(),
                schedule.getTrainingClass().getName(), schedule.getTitle(), schedule.getStartsAt(),
                schedule.getEndsAt(), schedule.getType());
    }
}