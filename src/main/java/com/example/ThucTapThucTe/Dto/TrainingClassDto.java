package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.training_class;
import java.util.List;

public record TrainingClassDto(Long id, String name, String term, String department, String major,
        usersdto mentor, List<usersdto> students) {

    public static TrainingClassDto from(training_class trainingClass) {
        usersdto mentor = trainingClass.getMentor() == null ? null : usersdto.from(trainingClass.getMentor());
        List<usersdto> students = trainingClass.getStudents().stream().map(usersdto::from).toList();
        return new TrainingClassDto(trainingClass.getId(), trainingClass.getName(), trainingClass.getTerm(),
                trainingClass.getDepartment(), trainingClass.getMajor(), mentor, students);
    }
}