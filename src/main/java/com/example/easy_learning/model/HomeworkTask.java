package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "homework_task")
@Data
public class HomeworkTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
}