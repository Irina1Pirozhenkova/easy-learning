package com.example.easy_learning.dto;

import lombok.Data;

public class StudentsHomeworkSDto {
    private Integer id;

    private StudentNRDto student;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;
}
