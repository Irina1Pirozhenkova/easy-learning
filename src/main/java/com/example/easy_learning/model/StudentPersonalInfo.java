package com.example.easy_learning.model;

import jakarta.persistence.Column;
import lombok.*;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class StudentPersonalInfo {
  @Column(name = "firstname")
  private String firstname;

  @Column(name = "lastname")
  private String lastname;

  @Column(name = "birthdate")
  private LocalDate birthdate;

  @Column(name = "class")
  private String className; // Используем "className", так как "class" — зарезервированное слово

  @Column(name = "subject")
  private String subject;

  @Column(name = "phone", unique = true)
  private String phone;

  @Column(name = "telegram", unique = true)
  private String telegram;
}
