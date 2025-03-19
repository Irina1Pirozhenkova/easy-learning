package com.example.easy_learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;

@Embeddable
@Data
public class StudentPersonalInfo {
  @Column(name = "firstname", nullable = false)
  private String firstname;

  @Column(name = "lastname", nullable = false)
  private String lastname;

  @Column(name = "birthdate", nullable = false)
  private LocalDate birthdate;

  @Column(name = "class", nullable = false)
  private String className; // Используем "className", так как "class" — зарезервированное слово

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "phone", nullable = false, unique = true)
  private String phone;

  @Column(name = "telegram", unique = true)
  private String telegram;
}
