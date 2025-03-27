package com.example.easy_learning.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentsHomeworkSDto {
    private Integer id;

    private StudentNRDto student;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;
}
