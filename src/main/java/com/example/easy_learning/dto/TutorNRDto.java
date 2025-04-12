package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class TutorNRDto {

  private Integer id;

  private TutorPersonalInfoDto personalInfo;

  private String email;

  private String password;
}
