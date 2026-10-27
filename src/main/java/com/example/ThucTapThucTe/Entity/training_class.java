package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
public class training_class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String term;
    private String department;
    private String major;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private User mentor;

        @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "training_class_students",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<User> students = new HashSet<>();

    protected training_class() {
    }

    public training_class(String name, String term, String department, String major) {
        this.name = name;
        this.term = term;
        this.department = department;
        this.major = major;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getTerm() { return term; }
    public String getDepartment() { return department; }
    public String getMajor() { return major; }
    public User getMentor() { return mentor; }
    public Set<User> getStudents() { return students; }
    public void setMentor(User mentor) { this.mentor = mentor; }
    public void addStudent(User student) { this.students.add(student); }
}