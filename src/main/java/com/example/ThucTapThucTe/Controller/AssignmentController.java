package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.AssignmentDto;
import com.example.ThucTapThucTe.Entity.Assignment;
import com.example.ThucTapThucTe.Entity.AssignmentType;
import com.example.ThucTapThucTe.Service.AssignmentService;
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
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService service;
    public AssignmentController(AssignmentService service) { this.service = service; }
    @GetMapping("/class/{classId}") public List<AssignmentDto> findByClass(@PathVariable Long classId) { return service.findByClass(classId).stream().map(AssignmentDto::from).toList(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentDto create(@RequestBody CreateAssignmentRequest request) {
        return AssignmentDto.from(service.create(request.classId(), request.title(), request.description(), request.deadline(),
                request.type() == null ? AssignmentType.FILE : request.type(), request.language(), request.durationMinutes()));
    }
    public record CreateAssignmentRequest(Long classId, String title, String description, LocalDateTime deadline,
            AssignmentType type, String language, Integer durationMinutes) { }
}