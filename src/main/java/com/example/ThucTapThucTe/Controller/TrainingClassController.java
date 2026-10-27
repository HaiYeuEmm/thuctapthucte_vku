package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.TrainingClassDto;
import com.example.ThucTapThucTe.Service.TrainingClassService;
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
@RequestMapping("/api/classes")
public class TrainingClassController {
    private final TrainingClassService service;

    public TrainingClassController(TrainingClassService service) { this.service = service; }
    @GetMapping 
    public List<TrainingClassDto> findAll() {
        return service.findAll().stream().map(TrainingClassDto::from).toList(); 
    }
    @GetMapping("/{id}") public TrainingClassDto findById(@PathVariable Long id) { return TrainingClassDto.from(service.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingClassDto create(@RequestBody CreateClassRequest request) {
        return TrainingClassDto.from(service.create(request.name(), request.term(), request.department(), request.major()));
    }

    @PostMapping("/{classId}/mentor/{mentorId}")
    public TrainingClassDto assignMentor(@PathVariable Long classId, @PathVariable Long mentorId) {
        return TrainingClassDto.from(service.assignMentor(classId, mentorId));
    }

    @PostMapping("/{classId}/students/{studentId}")
    public TrainingClassDto addStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        return TrainingClassDto.from(service.addStudent(classId, studentId));
    }

    public record CreateClassRequest(String name, String term, String department, String major) { }
}