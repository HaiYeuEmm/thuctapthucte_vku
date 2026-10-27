package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.QuizQuestionDto;
import com.example.ThucTapThucTe.Dto.SubmissionDto;
import com.example.ThucTapThucTe.Service.SubmissionService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService service;

    public SubmissionController(SubmissionService service) { this.service = service; }

    @GetMapping("/assignment/{assignmentId}")
    public List<SubmissionDto> byAssignment(@PathVariable Long assignmentId) {
        return service.findByAssignment(assignmentId).stream().map(SubmissionDto::from).toList();
    }

    @GetMapping("/student/{studentId}")
    public List<SubmissionDto> byStudent(@PathVariable Long studentId) {
        return service.findByStudent(studentId).stream().map(SubmissionDto::from).toList();
    }

    @GetMapping("/quiz/{assignmentId}/questions")
    public List<QuizQuestionDto> questions(@PathVariable Long assignmentId) {
        return service.findQuestions(assignmentId).stream().map(QuizQuestionDto::from).toList();
    }

    @PostMapping("/quiz/{assignmentId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizQuestionDto addQuestion(@PathVariable Long assignmentId, @RequestBody CreateQuestionRequest request) {
        return QuizQuestionDto.from(service.addQuestion(assignmentId, request.questionText(), request.optionA(), request.optionB(),
                request.optionC(), request.optionD(), request.correctOption(), request.points()));
    }

    @PostMapping("/quiz/{assignmentId}/questions/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<QuizQuestionDto> addQuestions(@PathVariable Long assignmentId,
            @RequestBody List<CreateQuestionRequest> requests) {
        if (requests == null) throw new IllegalArgumentException("Quiz phai co it nhat mot cau hoi");
        List<SubmissionService.QuizQuestionInput> inputs = requests.stream()
                .map(request -> request == null ? null : new SubmissionService.QuizQuestionInput(
                        request.questionText(), request.optionA(), request.optionB(), request.optionC(),
                        request.optionD(), request.correctOption(), request.points()))
                .toList();
        return service.addQuestions(assignmentId, inputs).stream().map(QuizQuestionDto::from).toList();
    }

    @PostMapping(value = "/file", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submitFile(@RequestParam Long assignmentId, @RequestParam Long studentId,
            @RequestParam("file") MultipartFile file) {
        return SubmissionDto.from(service.submitFile(assignmentId, studentId, file));
    }

    @PostMapping("/code")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submitCode(@RequestBody CodeSubmissionRequest request) {
        return SubmissionDto.from(service.submitCode(request.assignmentId(), request.studentId(), request.code()));
    }

    @PostMapping("/quiz/{assignmentId}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submitQuiz(@PathVariable Long assignmentId, @RequestBody QuizSubmissionRequest request) {
        return SubmissionDto.from(service.submitQuiz(assignmentId, request.studentId(), request.answers()));
    }

    @PostMapping("/{submissionId}/grade")
    public SubmissionDto grade(@PathVariable Long submissionId, @RequestBody GradeRequest request) {
        return SubmissionDto.from(service.grade(submissionId, request.mentorId(), request.score(), request.feedback()));
    }

    public record CreateQuestionRequest(String questionText, String optionA, String optionB, String optionC,
            String optionD, String correctOption, Double points) { }
    public record CodeSubmissionRequest(Long assignmentId, Long studentId, String code) { }
    public record QuizSubmissionRequest(Long studentId, Map<Long, String> answers) { }
    public record GradeRequest(Long mentorId, Double score, String feedback) { }
}