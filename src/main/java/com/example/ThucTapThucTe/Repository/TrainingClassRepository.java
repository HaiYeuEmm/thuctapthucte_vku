package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.training_class;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface TrainingClassRepository extends JpaRepository<training_class, Long> {
    @Query("SELECT DISTINCT c FROM training_class c LEFT JOIN FETCH c.students s")
    List<training_class> findAllWithStudents();

    @Query("SELECT DISTINCT c FROM training_class c LEFT JOIN FETCH c.students s WHERE c.id = :id")
    java.util.Optional<training_class> findByIdWithStudents(@Param("id") Long id);
}