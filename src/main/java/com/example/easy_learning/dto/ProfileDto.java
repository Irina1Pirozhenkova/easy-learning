package com.example.easy_learning.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileDto {
  private Integer id;
  private String email;
  private String password;
  private String firstname;
  private String lastname;
  private LocalDate birthdate;
  private String phone;
  private String telegram;
}
