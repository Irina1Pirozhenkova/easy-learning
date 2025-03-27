package com.example.easy_learning.dto;

import lombok.Data;

import java.util.Set;

@Data
public class HomeworkNRDto {

  private Integer id;

  private String className; // Используем "className" вместо "class"

  private String subject;

  private String topic;

  private Integer difficulty;
}
