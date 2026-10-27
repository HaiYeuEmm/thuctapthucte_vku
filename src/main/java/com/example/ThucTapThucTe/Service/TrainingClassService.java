package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.training_class;
import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.TrainingClassRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrainingClassService {

    private final TrainingClassRepository repository;
    private final usersservice usersService;

    public TrainingClassService(TrainingClassRepository repository, usersservice usersService) {
        this.repository = repository;
        this.usersService = usersService;
    }

    public List<training_class> findAll() { return repository.findAllWithStudents(); }

    public training_class findById(Long id) {
        return repository.findByIdWithStudents(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay lop thuc tap voi ma " + id));
    }

    public training_class create(String name, String term, String department, String major) {
        if (name == null || name.isBlank() || term == null || term.isBlank()
                || department == null || department.isBlank() || major == null || major.isBlank()) {
            throw new IllegalArgumentException("Ten lop, ky, bo phan va chuyen nganh khong duoc de trong");
        }
        return repository.save(new training_class(name, term, department, major));
    }

    public training_class assignMentor(Long classId, Long mentorId) {
        training_class trainingClass = findById(classId);
        trainingClass.setMentor(usersService.requireRole(mentorId, UserRole.MENTOR));
        return repository.save(trainingClass);
    }

    public training_class addStudent(Long classId, Long studentId) {
        training_class trainingClass = findById(classId);
        User student = usersService.requireRole(studentId, UserRole.STUDENT);
        trainingClass.addStudent(student);
        return repository.save(trainingClass);
    }

    public List<training_class> getall(){
        return repository.findAllWithStudents();
    }
}