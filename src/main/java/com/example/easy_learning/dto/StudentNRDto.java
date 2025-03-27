package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class StudentNRDto {

  private Integer id;

  private StudentPersonalInfoDto studentPersonalInfo;

  private String password;
}
