package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "homework")
@Getter
@Setter
@NoArgsConstructor
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "class", nullable = false)
    private ClassLevel className; // Используем "className" вместо "class"

    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false)
    private Subject subject;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @OneToMany(mappedBy = "homework", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsHomework> students = new HashSet<>();

    @OneToMany(mappedBy = "homework", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HomeworkTask> tasks = new HashSet<>();

    public void setTasks(Set<HomeworkTask> tasks) {
        this.tasks.clear();
        tasks.forEach(task -> task.setHomework(this));
    }

    public void setStudents(Set<StudentsHomework> students) {
        this.students.clear();
        students.forEach(student -> student.setHomework(this));
    }
}