package com.example.easy_learning.dto;

import lombok.Data;

import java.util.Set;

@Data
public class StudentRDto {

  private Integer id;

  private String password;

  private StudentPersonalInfoDto studentPersonalInfo;

  private String email;

  private Set<StudentsTutorsTDto> tutors;
}
