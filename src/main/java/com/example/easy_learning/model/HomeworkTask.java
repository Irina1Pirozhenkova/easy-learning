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

    public void setTask(Task task) {
        this.task = task;
        if (task != null) task.getHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null) homework.getTasks().add(this);
    }
}