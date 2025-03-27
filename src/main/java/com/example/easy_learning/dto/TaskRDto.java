package com.example.easy_learning.dto;

import lombok.Data;

import java.util.Set;

@Data
public class TaskRDto {

  private Integer id;

  private String photoUrl;

  private String className;

  private String subject;

  private String topic;

  private Integer difficulty;

  private TutorNRDto tutor;

  private Set<HomeworkTaskHDto> homeworks;
}
