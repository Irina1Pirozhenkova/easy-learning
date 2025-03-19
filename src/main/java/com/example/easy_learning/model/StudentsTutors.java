package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "students_tutors")
@Data
public class StudentsTutors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;
}