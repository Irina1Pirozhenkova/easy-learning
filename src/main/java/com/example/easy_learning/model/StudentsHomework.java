package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "students_homework")
@Data
public class StudentsHomework {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;
}