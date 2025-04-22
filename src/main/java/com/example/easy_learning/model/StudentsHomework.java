package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Data;

@Entity
@Table(name = "students_homework")
@Getter
@Setter
@NoArgsConstructor
public class StudentsHomework {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;

    public void setStudent(User student) {
        this.student = student;
        if (student != null && student.getHomeworks() != null) student.getStudentHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null && homework.getStudents() != null) homework.getStudents().add(this);
    }
}