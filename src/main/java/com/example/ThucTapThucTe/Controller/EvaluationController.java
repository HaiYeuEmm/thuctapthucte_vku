package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.EvaluationDto;
import com.example.ThucTapThucTe.Entity.Evaluation;
import com.example.ThucTapThucTe.Service.EvaluationService;
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
@RequestMapping("/api/evaluations")
public class EvaluationController {
    private final EvaluationService service;
    public EvaluationController(EvaluationService service) { this.service = service; }
    @GetMapping("/class/{classId}") public List<EvaluationDto> findByClass(@PathVariable Long classId) { return service.findByClass(classId).stream().map(EvaluationDto::from).toList(); }
    @GetMapping("/student/{studentId}") public List<EvaluationDto> findByStudent(@PathVariable Long studentId) { return service.findByStudent(studentId).stream().map(EvaluationDto::from).toList(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationDto create(@RequestBody CreateEvaluationRequest request) {
        return EvaluationDto.from(service.create(request.classId(), request.studentId(), request.processScore(), request.testScore(),
                request.attendanceScore(), request.finalScore(), request.feedback()));
    }

    @PostMapping("/{id}/approve")
    public EvaluationDto approve(@PathVariable Long id) { return EvaluationDto.from(service.approve(id)); }

    public record CreateEvaluationRequest(Long classId, Long studentId, Double processScore, Double testScore,
            Double attendanceScore, Double finalScore, String feedback) { }
}