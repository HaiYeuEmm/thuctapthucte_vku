package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.ScheduleDto;
import com.example.ThucTapThucTe.Entity.ScheduleType;
import com.example.ThucTapThucTe.Service.ScheduleService;
import java.time.LocalDateTime;
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
@RequestMapping("/api/schedules")
public class ScheduleController {
    private final ScheduleService service;
    public ScheduleController(ScheduleService service) { this.service = service; }
    @GetMapping("/class/{classId}") public List<ScheduleDto> findByClass(@PathVariable Long classId) { return service.findByClass(classId).stream().map(ScheduleDto::from).toList(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleDto create(@RequestBody CreateScheduleRequest request) {
        return ScheduleDto.from(service.create(request.classId(), request.title(), request.startsAt(), request.endsAt(), request.type()));
    }
    public record CreateScheduleRequest(Long classId, String title, LocalDateTime startsAt, LocalDateTime endsAt, ScheduleType type) { }
}