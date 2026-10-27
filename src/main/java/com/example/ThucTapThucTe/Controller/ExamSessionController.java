package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.ExamSessionDto;
import com.example.ThucTapThucTe.Service.ExamSessionService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
public class ExamSessionController {
    private final ExamSessionService service;

    public ExamSessionController(ExamSessionService service) { this.service = service; }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamSessionDto start(@RequestBody StartRequest request) {
        return service.start(request.assignmentId(), request.studentId());
    }

    @PostMapping("/{sessionId}/violation")
    public ExamSessionDto flag(@PathVariable Long sessionId, @RequestBody ViolationRequest request) {
        return service.flag(sessionId, request.studentId(), request.reason());
    }

    @GetMapping("/student/{studentId}/active")
    public Optional<ExamSessionDto> active(@PathVariable Long studentId) {
        return service.activeForStudent(studentId);
    }

    @GetMapping("/{sessionId}/student/{studentId}")
    public ExamSessionDto getForStudent(@PathVariable Long sessionId, @PathVariable Long studentId) {
        return service.getForStudent(sessionId, studentId);
    }

    @GetMapping("/class/{classId}")
    public List<ExamSessionDto> forClass(@PathVariable Long classId, @RequestParam Long mentorId) {
        return service.findForClass(classId, mentorId);
    }

    @PostMapping("/{sessionId}/decision")
    public ExamSessionDto decide(@PathVariable Long sessionId, @RequestBody DecisionRequest request) {
        return service.decide(sessionId, request.mentorId(), request.decision());
    }

    @PostMapping("/{assignmentId}/student/{studentId}/complete")
    public void complete(@PathVariable Long assignmentId, @PathVariable Long studentId) {
        service.complete(assignmentId, studentId);
    }

    public record StartRequest(Long assignmentId, Long studentId) { }
    public record ViolationRequest(Long studentId, String reason) { }
    public record DecisionRequest(Long mentorId, String decision) { }
}
