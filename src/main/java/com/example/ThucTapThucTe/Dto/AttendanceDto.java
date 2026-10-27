package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.Attendance;
import com.example.ThucTapThucTe.Entity.AttendanceStatus;
import java.time.LocalDate;

public record AttendanceDto(Long id, Long classId, Long studentId, String studentName,
        LocalDate date, AttendanceStatus status) {

    public static AttendanceDto from(Attendance attendance) {
        return new AttendanceDto(attendance.getId(), attendance.getTrainingClass().getId(),
                attendance.getStudent().getId(), attendance.getStudent().getFullName(),
                attendance.getAttendanceDate(), attendance.getStatus());
    }
}