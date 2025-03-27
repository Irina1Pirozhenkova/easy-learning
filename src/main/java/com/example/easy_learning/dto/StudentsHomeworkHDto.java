package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class StudentsHomeworkHDto {
    private Integer id;

    private HomeworkNRDto homework;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;
}
