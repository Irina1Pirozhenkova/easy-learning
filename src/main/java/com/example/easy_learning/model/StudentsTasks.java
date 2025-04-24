package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students_tasks")
@Getter
@Setter
@NoArgsConstructor
public class StudentsTasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;

    public void setStudent(User student) {
        this.student = student;
        if (student != null && student.getStudentsTasks() != null) student.getStudentsTasks().add(this);
    }

    public void setTask(Task task) {
        this.task = task;
        if (task != null && task.getStudentsTasks() != null) task.getStudentsTasks().add(this);
    }
}