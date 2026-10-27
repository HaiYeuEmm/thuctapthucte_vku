package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.AttendanceDto;
import com.example.ThucTapThucTe.Entity.Attendance;
import com.example.ThucTapThucTe.Entity.AttendanceStatus;
import com.example.ThucTapThucTe.Service.AttendanceService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService service;
    public AttendanceController(AttendanceService service) { this.service = service; }
    @GetMapping("/class/{classId}") public List<AttendanceDto> findByClass(@PathVariable Long classId) { return service.findByClass(classId).stream().map(AttendanceDto::from).toList(); }
    @GetMapping("/student/{studentId}") public List<AttendanceDto> findByStudent(@PathVariable Long studentId) { return service.findByStudent(studentId).stream().map(AttendanceDto::from).toList(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceDto create(@RequestBody CreateAttendanceRequest request) {
        return AttendanceDto.from(service.create(request.classId(), request.studentId(), request.date(), request.status()));
    }
    public record CreateAttendanceRequest(Long classId, Long studentId, LocalDate date, AttendanceStatus status) { }
}