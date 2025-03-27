package com.example.easy_learning.dto;

import java.util.Set;

public class TutorRDto {
  private Integer id;

  private TutorPersonalInfoDto personalInfo;

  private String password;

  private Set<TaskNRDto> tasks;

  private Set<HomeworkNRDto> homeworks;

  private Set<StudentNRDto> students;
}
