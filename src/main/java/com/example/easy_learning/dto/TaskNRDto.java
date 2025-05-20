package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class TaskNRDto {
  private Integer id;
  private String photoUrl;
  private String className;
  private String subject;
  private String description;
  private String topic;
  private Integer difficulty;
  private Integer tutorId;
}
