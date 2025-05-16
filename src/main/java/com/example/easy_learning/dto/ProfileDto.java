package com.example.easy_learning.dto;

import lombok.Data;

import java.time.LocalDate;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;



@Data
public class ProfileDto {
  private Integer id;

  @Email(message = "Некорректный email")
  private String email;

  private String password;
  private String firstname;
  private String lastname;
  private LocalDate birthdate;

  @Pattern(regexp = "\\d{11}", message = "Телефон должен состоять из 11 цифр")
  private String phone;

  private String telegram;
}
