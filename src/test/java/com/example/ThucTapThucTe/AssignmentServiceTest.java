package com.example.ThucTapThucTe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.ThucTapThucTe.Entity.Assignment;
import com.example.ThucTapThucTe.Entity.AssignmentType;
import com.example.ThucTapThucTe.Entity.training_class;
import com.example.ThucTapThucTe.Repository.AssignmentRepository;
import com.example.ThucTapThucTe.Service.AssignmentService;
import com.example.ThucTapThucTe.Service.RealtimeUpdatePublisher;
import com.example.ThucTapThucTe.Service.TrainingClassService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AssignmentServiceTest {

    @Test
    void createTimedExamStoresDurationAndFixedMaximumScore() {
        AssignmentRepository repository = mock(AssignmentRepository.class);
        TrainingClassService classService = mock(TrainingClassService.class);
        RealtimeUpdatePublisher realtimePublisher = mock(RealtimeUpdatePublisher.class);
        when(classService.findById(1L)).thenReturn(new training_class("Class", "2026", "Dept", "Major"));
        when(repository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AssignmentService service = new AssignmentService(repository, classService, realtimePublisher);

        Assignment assignment = service.create(1L, "Quiz", null, LocalDateTime.now().plusDays(1),
                AssignmentType.QUIZ, null, 45);

        assertEquals(45, assignment.getDurationMinutes());
        assertEquals(10.0, assignment.getMaxScore());
    }

    @Test
    void createTimedExamRequiresDurationWithinConfiguredRange() {
        AssignmentService service = new AssignmentService(mock(AssignmentRepository.class),
                mock(TrainingClassService.class), mock(RealtimeUpdatePublisher.class));

        assertThrows(IllegalArgumentException.class, () -> service.create(1L, "Quiz", null,
                LocalDateTime.now().plusDays(1), AssignmentType.QUIZ, null, 0));
    }
}
