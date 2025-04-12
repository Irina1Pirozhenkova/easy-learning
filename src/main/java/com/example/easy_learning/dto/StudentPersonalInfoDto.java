package com.example.easy_learning.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentPersonalInfoDto {

  private String firstname;

  private String lastname;

  private LocalDate birthdate;

  private String className;

  private String subject;

  private String phone;

  private String telegram;
}
